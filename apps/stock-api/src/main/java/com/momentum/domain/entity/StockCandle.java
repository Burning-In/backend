package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPivotType;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPriceTrend;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockCandle extends BaseEntity {

  private LocalDate tradeDate;
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

  private StockCandle(LocalDate tradeDate, Long openPrice, Long highPrice, Long lowPrice, Long closePrice, Long volume,
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

  public static StockCandle daily(
      Stock stock,
      String rawDate,
      long openPrice,
      long highPrice,
      long lowPrice,
      long closePrice,
      long volume,
      String priceChangeSign) {
    return new StockCandle(
        LocalDate.parse(rawDate, DateTimeFormatter.BASIC_ISO_DATE),
        openPrice,
        highPrice,
        lowPrice,
        closePrice,
        volume,
        StockPriceTrend.getValue(priceChangeSign),
        StockPivotType.UNDEFINED,
        StockCandlePeriod.DAY,
        stock
    );
  }

  public void updatePivotType(StockPivotType stockPivotType) {
    if (this.stockPivotType != stockPivotType) {
      this.stockPivotType = stockPivotType;
    }
  }
}
