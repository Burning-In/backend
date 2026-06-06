package com.momentum.domain.stockcandle;

import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository {

  List<StockDailyCandle> saveAll(List<StockDailyCandle> candles);

  StockDailyCandle save(StockDailyCandle candle);

  Optional<StockDailyCandle> findLastCandleAfterDate(Stock stock, LocalDate tradeDate);

  Long averageVolume(Stock stock, LocalDate from, LocalDate to);

  List<StockDailyCandle> findRecentCandles(Long stockId, LocalDate baseDate, int limit);

  Optional<StockDailyCandle> findRecentCandle(Stock stock, LocalDate date);

  /** 종목의 캔들을 tradeDate 오름차순으로 조회한다. from/to가 null이면 해당 경계는 무시한다. */
  List<StockDailyCandle> findByStockAndDateRange(Stock stock, LocalDate from, LocalDate to);
}
