package com.momentum.domain.stocktick;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockRealtimeRegimeService {

  private static final double THRESHOLD_PERCENT = 5.0;

  private final StockRepository stockRepository;
  private final StockRealtimeRegimePolicy stockRealtimeRegimePolicy;
  private final StockBaseRepository stockBaseRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;

  @Transactional
  public void resolveRealtimeRegime(String stockCode, long currentPrice) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);
    StockBase currentBase = stockBaseRepository.findCurrentBaseWithLines(stock)
        .orElseThrow(IllegalStateException::new);
    StockAnchorPoint lastAnchorPoint = stockAnchorPointRepository.findLastStockAnchorPoint(stock)
        .orElseThrow(IllegalStateException::new);

    StockRegime newRegime = stockRealtimeRegimePolicy.decide(currentPrice, stock, currentBase, lastAnchorPoint,
        THRESHOLD_PERCENT);
    stock.update(newRegime);
    stockRepository.save(stock);
  }
}
