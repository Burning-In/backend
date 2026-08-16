package com.momentum.interfaces.api.stock;

import com.momentum.domain.movingaverage.StockMovingAveragePeriod;
import java.time.LocalDate;
import java.util.List;

public class StockChartV1Dto {

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

  public record MovingAverageResponse(
      StockMovingAveragePeriod period,
      List<MovingAverageItem> dataPoints
  ) {

    public record MovingAverageItem(
        LocalDate tradeDate,
        Long price
    ) {

    }
  }

  public record BaseListResponse(
      List<BaseItem> bases
  ) {

    public record BaseItem(
        LocalDate startDate,
        LocalDate endDate,
        Long supportPrice,
        Long resistancePrice
    ) {

    }
  }
}
