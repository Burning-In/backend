package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.indicator.StockPivotType;
import com.momentum.domain.entity.indicator.StockPriceTrend;
import com.momentum.infrastructure.api.dto.StockChartInfoResponse.CandleResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockCandle extends BaseEntity {

  private String tradeDate;
  private Long openPrice;
  private Long highPrice;
  private Long lowPrice;
  private Long closePrice;
  private Long volume;

  @Enumerated(EnumType.STRING)
  private StockPriceTrend stockPriceTrend;

  @Enumerated(EnumType.STRING)
  private StockPivotType stockPivotType;

  @Enumerated(EnumType.STRING)
  private StockCandlePeriod candlePeriod;

  @ManyToOne
  private Stock stock;

  private StockCandle(String tradeDate, Long openPrice, Long highPrice, Long lowPrice, Long closePrice, Long volume,
      StockPriceTrend stockPriceTrend, StockPivotType stockPivotType,
      StockCandlePeriod candlePeriod, Stock stock) {
    this.tradeDate = tradeDate;
    this.openPrice = openPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.closePrice = closePrice;
    this.volume = volume;
    this.stockPriceTrend = stockPriceTrend;
    this.stockPivotType = stockPivotType;
    this.candlePeriod = candlePeriod;
    this.stock = stock;
  }

  public static StockCandle daily(Stock stock, CandleResponse candleResponse) {
    return new StockCandle(
        candleResponse.date(),
        candleResponse.openPrice(),
        candleResponse.highPrice(),
        candleResponse.lowPrice(),
        candleResponse.closePrice(),
        candleResponse.volume(),
        StockPriceTrend.getValue(candleResponse.priceChangeSign()),
        StockPivotType.UNDEFINED,
        StockCandlePeriod.DAY,
        stock
    );
  }
}
