package com.momentum.application.dto;

import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPivotType;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPriceTrend;
import lombok.Builder;

@Builder
public record StockCandleInfo(
    Long openPrice,
    Long highPrice,
    Long lowPrice,
    Long closePrice,
    Long volume,
    StockPriceTrend stockPriceTrend,
    StockPivotType stockPivotType,
    Long stockId
) {

  public static StockCandleInfo from(StockCandle entity) {
    return StockCandleInfo.builder()
        .openPrice(entity.getOpenPrice())
        .highPrice(entity.getHighPrice())
        .lowPrice(entity.getLowPrice())
        .closePrice(entity.getClosePrice())
        .volume(entity.getVolume())
        .stockPriceTrend(entity.getStockPriceTrend())
        .stockPivotType(entity.getStockPivotType())
        .stockId(entity.getStock().getId())
        .build();
  }
}
