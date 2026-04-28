package com.momentum.application.dto;

import com.momentum.domain.stockcandle.StockCandleTrend;
import com.momentum.domain.stockcandle.StockDailyCandle;
import lombok.Builder;

@Builder
public record StockCandleInfo(
    Long openPrice,
    Long highPrice,
    Long lowPrice,
    Long closePrice,
    Long volume,
    StockCandleTrend stockCandleTrend,
    Long stockId
) {

  public static StockCandleInfo from(StockDailyCandle entity) {
    return StockCandleInfo.builder()
        .openPrice(entity.getOpenPrice())
        .highPrice(entity.getHighPrice())
        .lowPrice(entity.getLowPrice())
        .closePrice(entity.getClosePrice())
        .volume(entity.getVolume())
        .stockCandleTrend(entity.getStockCandleTrend())
        .stockId(entity.getStock().getId())
        .build();
  }
}
