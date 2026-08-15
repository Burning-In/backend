package com.momentum.domain.stock;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockDailyRegimeService {

  private static final double BREAKOUT_THRESHOLD = 3.0;

  private final StockRepository stockRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockCandleRepository stockCandleRepository;
  private final StockDailyRegimePolicy stockDailyRegimePolicy;

  public void resolveDailyRegime(Stock stock, StockDailyCandle dailyCandle) {
    StockBase currentBase = stockBaseRepository.findCurrentBaseWithLines(stock)
        .orElseThrow(() -> new IllegalArgumentException("Stock Base not found"));
    StockAnchorPoint recentAnchorPoint = stockAnchorPointRepository.findLatestByStock(stock)
        .orElseThrow(() -> new IllegalArgumentException("No recent price point"));
    long baseAverageVolume = stockCandleRepository.averageVolume(stock,
        currentBase.getCreatedAt().toLocalDate(), dailyCandle.getTradeDate());

    StockRegime newRegime = stockDailyRegimePolicy.decide(dailyCandle.getClosePrice(), dailyCandle.getVolume(),
        baseAverageVolume, stock, currentBase, recentAnchorPoint, BREAKOUT_THRESHOLD);
    stock.update(newRegime);
    stockRepository.save(stock);
  }
}
