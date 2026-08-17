package com.momentum.domain.stockcandle;

import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository {

  List<StockDailyCandle> saveAll(List<StockDailyCandle> candles);

  StockDailyCandle save(StockDailyCandle candle);

  Optional<StockDailyCandle> findLastCandleBeforeDate(Stock stock, LocalDate tradeDate);

  Long averageVolume(Stock stock, LocalDate from, LocalDate to);

  List<StockDailyCandle> findRecentCandles(Long stockId, LocalDate baseDate, int limit);

  Optional<StockDailyCandle> findRecentCandle(Stock stock, LocalDate date);
}
