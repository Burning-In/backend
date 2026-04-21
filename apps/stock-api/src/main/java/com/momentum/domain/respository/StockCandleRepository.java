package com.momentum.domain.respository;

import com.momentum.domain.entity.stock.Stock;
import com.momentum.domain.entity.stock.StockDailyCandle;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository {

  List<StockDailyCandle> saveAll(List<StockDailyCandle> candles);

  StockDailyCandle save(StockDailyCandle candle);

  Optional<StockDailyCandle> findByStockAndDate(Stock stock, LocalDate tradeDate);

  Long findAvgVolumeByStockAndDateAfter(Stock stock, LocalDate oneYearAgo);
}
