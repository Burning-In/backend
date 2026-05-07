package com.momentum.domain.ma;

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

  private static final int MAX_PERIOD = 200;

  private final StockMovingAverageRepository stockMovingAverageRepository;
  private final StockCandleRepository stockCandleRepository;

  public List<StockMovingAverage> create(Stock stock) {
    List<StockDailyCandle> candles = stockCandleRepository.findRecentCandles(
        stock.getId(), LocalDate.now(), MAX_PERIOD
    );

    List<StockMovingAverage> result = new ArrayList<>();
    for (StockMovingAveragePeriod period : StockMovingAveragePeriod.values()) {
      if (candles.size() < period.getPeriod()) {
        continue;
      }
      long ma = (long) candles.subList(0, period.getPeriod())
          .stream()
          .mapToLong(StockDailyCandle::getClosePrice)
          .average()
          .orElseThrow();
      result.add(new StockMovingAverage(ma, period, stock));
    }

    return stockMovingAverageRepository.saveAll(result);
  }
}
