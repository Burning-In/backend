package com.momentum.domain.respository;

import com.momentum.domain.entity.StockCandle;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository {

  List<StockCandle> saveAll(List<StockCandle> candles);

  StockCandle save(StockCandle candle);

  Optional<StockCandle> findDailyCandle(Long stockId, LocalDate tradeDate);
}
