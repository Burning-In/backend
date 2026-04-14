package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.indicator.price.StockPriceTrend;
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
public class StockDailyCandle extends BaseEntity {

  private LocalDate tradeDate;
  private long openPrice;
  private long highPrice;
  private long lowPrice;
  private long closePrice;
  private long volume;

  @Enumerated(EnumType.STRING)
  private StockPriceTrend stockPriceTrend;
  @ManyToOne
  private Stock stock;

  private StockDailyCandle(LocalDate tradeDate, Long openPrice, Long highPrice, Long lowPrice, Long closePrice, Long volume,
      StockPriceTrend stockPriceTrend, Stock stock) {
    this.tradeDate = tradeDate;
    this.openPrice = openPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.closePrice = closePrice;
    this.volume = volume;
    this.stockPriceTrend = stockPriceTrend;
    this.stock = stock;
  }

  public static StockDailyCandle create(
      Stock stock,
      String rawDate,
      long openPrice,
      long highPrice,
      long lowPrice,
      long closePrice,
      long volume,
      String priceChangeSign) {
    return new StockDailyCandle(
        LocalDate.parse(rawDate, DateTimeFormatter.BASIC_ISO_DATE),
        openPrice,
        highPrice,
        lowPrice,
        closePrice,
        volume,
        StockPriceTrend.getValue(priceChangeSign),
        stock
    );
  }
}
