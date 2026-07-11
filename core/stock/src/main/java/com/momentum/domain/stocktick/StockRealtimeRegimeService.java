package com.momentum.domain.stocktick;

import com.momentum.domain.pricepoint.service.StockPricePointTypeDecider;
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

  private final StockPricePointTypeDecider stockPricePointTypeDecider;
  private final StockRealtimeRegimePolicy stockRealtimeRegimePolicy;

  @Transactional
  public void resolveRealtimeRegime(String stockCode, long currentPrice) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);
    stockPricePointTypeDecider.resolvePointTypes(stock);

    StockRegime newRegime = stockRealtimeRegimePolicy.determine(currentPrice, stock, BREAKOUT_THRESHOLD_PERCENT);
    stock.update(newRegime);
    stockRepository.save(stock);
  }
}
