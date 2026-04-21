package com.momentum.application.dto;

import com.momentum.domain.entity.stock.StockDailyCandle;
import com.momentum.domain.entity.analysis.pivot.StockPriceTrend;
import lombok.Builder;

@Builder
public record StockCandleInfo(
    Long openPrice,
    Long highPrice,
    Long lowPrice,
    Long closePrice,
    Long volume,
    StockPriceTrend stockPriceTrend,
    Long stockId
) {

  public static StockCandleInfo from(StockDailyCandle entity) {
    return StockCandleInfo.builder()
        .openPrice(entity.getOpenPrice())
        .highPrice(entity.getHighPrice())
        .lowPrice(entity.getLowPrice())
        .closePrice(entity.getClosePrice())
        .volume(entity.getVolume())
        .stockPriceTrend(entity.getStockPriceTrend())
        .stockId(entity.getStock().getId())
        .build();
  }
}
