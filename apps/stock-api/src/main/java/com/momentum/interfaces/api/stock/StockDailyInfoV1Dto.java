package com.momentum.interfaces.api.stock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StockDailyInfoV1Dto {

  public record DailyCandleResponse(
      List<DailyCandle> candles
  ) {

    public record DailyCandle(
        LocalDate tradeDate,
        Long openPrice,
        Long highPrice,
        Long lowPrice,
        Long closePrice,
        Long volume
    ) {

    }
  }

  public record IntradayDailyCandleResponse(
      LocalDate tradeDate,
      LocalDateTime to,
      Long openPrice,
      Long highPrice,
      Long lowPrice,
      Long closePrice,
      Long volume
  ) {

  }
}
