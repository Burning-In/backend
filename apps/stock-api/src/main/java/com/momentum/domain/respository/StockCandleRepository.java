package com.momentum.domain.respository;

import com.momentum.domain.entity.StockDailyCandle;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository {

  List<StockDailyCandle> saveAll(List<StockDailyCandle> candles);

  StockDailyCandle save(StockDailyCandle candle);

  Optional<StockDailyCandle> findDailyCandle(Long stockId, LocalDate tradeDate);
}
