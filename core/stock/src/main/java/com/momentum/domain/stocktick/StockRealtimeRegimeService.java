package com.momentum.domain.stocktick;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.service.StockPricePointTypeDecider;
import com.momentum.domain.stock.RealtimeRegimePolicy;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockRealtimeRegimeService {

  private static final double BREAKOUT_THRESHOLD_PERCENT = 5.0;

  private final StockRepository stockRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointTypeDecider stockPricePointTypeDecider;
  private final RealtimeRegimePolicy realtimeRegimePolicy;

  @Transactional
  public void resolveRealtimeRegime(String stockCode, long currentPrice) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);
    stockPricePointTypeDecider.resolveType(stock);
    StockBase currentStockBase = stockBaseRepository.findCurrentBaseWithLines(stock)
        .orElseThrow(IllegalStateException::new);
    StockPricePoint lastPricePoint = stockPricePointRepository.findLastStockPricePoint(stock)
        .orElseThrow(IllegalStateException::new);

    StockRegime decided = realtimeRegimePolicy.decide(currentPrice, currentStockBase, stock,
        BREAKOUT_THRESHOLD_PERCENT, lastPricePoint.getPrice());
    stock.update(decided);
    stockRepository.save(stock);
  }
}
