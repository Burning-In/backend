package com.momentum.domain.movingaverage;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockMovingAverageService {

  private final StockMovingAverageRepository stockMovingAverageRepository;
  private final StockCandleRepository stockCandleRepository;

  public List<StockMovingAverage> create(Stock stock, LocalDate baseDate) {
    List<StockDailyCandle> candles = stockCandleRepository.findRecentCandles(
        stock.getId(), baseDate, StockMovingAveragePeriod.longestPeriod()
    );

    List<StockMovingAverage> movingAverages = new ArrayList<>();
    long sum = 0;
    int summedCount = 0;
    for (StockMovingAveragePeriod period : StockMovingAveragePeriod.ascending()) {
      if (candles.size() < period.getPeriod()) {
        continue;
      }
      while (summedCount < period.getPeriod()) {
        sum += candles.get(summedCount).getClosePrice();
        summedCount++;
      }
      movingAverages.add(new StockMovingAverage(sum / period.getPeriod(), baseDate, period, stock));
    }

    return stockMovingAverageRepository.saveAll(movingAverages);
  }
}
