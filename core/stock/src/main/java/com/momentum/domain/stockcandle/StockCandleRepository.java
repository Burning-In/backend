package com.momentum.domain.stockcandle;

import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository {

  List<StockDailyCandle> saveAll(List<StockDailyCandle> candles);

  StockDailyCandle save(StockDailyCandle candle);

  Optional<StockDailyCandle> findLastCandleAfterDate(Stock stock, LocalDate tradeDate);

  Long findAvgVolumeByStockAndDateAfter(Stock stock, LocalDate oneYearAgo);

  List<StockDailyCandle> findRecentCandles(Long stockId, LocalDate baseDate, int limit);
}
