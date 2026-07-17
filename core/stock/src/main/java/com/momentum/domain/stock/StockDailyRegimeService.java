package com.momentum.domain.stock;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
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
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;
  private final StockDailyRegimePolicy stockDailyRegimePolicy;

  public void resolveDailyRegime(Stock stock, StockDailyCandle dailyCandle) {
    StockBase currentBase = stockBaseRepository.findCurrentBaseWithLines(stock)
        .orElseThrow(() -> new IllegalArgumentException("Stock Base not found"));
    StockPricePoint recentPricePoint = stockPricePointRepository.findLatestByStock(stock)
        .orElseThrow(() -> new IllegalArgumentException("No recent price point"));
    long baseAverageVolume = stockCandleRepository.averageVolume(stock,
        currentBase.getCreatedAt().toLocalDate(), dailyCandle.getTradeDate());

    StockRegime newRegime = stockDailyRegimePolicy.decide(dailyCandle.getClosePrice(), dailyCandle.getVolume(),
        baseAverageVolume, stock, currentBase, recentPricePoint, BREAKOUT_THRESHOLD);
    stock.update(newRegime);
    stockRepository.save(stock);
  }
}
