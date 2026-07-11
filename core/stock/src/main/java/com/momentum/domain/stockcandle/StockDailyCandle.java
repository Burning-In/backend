package com.momentum.domain.stockcandle;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.Entity;
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

  @ManyToOne
  private Stock stock;

  private StockDailyCandle(LocalDate tradeDate, Long openPrice, Long highPrice, Long lowPrice, Long closePrice, Long volume, Stock stock) {
    this.tradeDate = tradeDate;
    this.openPrice = openPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.closePrice = closePrice;
    this.volume = volume;
    this.stock = stock;
  }

  public static StockDailyCandle create(
      Stock stock,
      String rawDate,
      long openPrice,
      long highPrice,
      long lowPrice,
      long closePrice,
      long volume) {
    return new StockDailyCandle(
        LocalDate.parse(rawDate, DateTimeFormatter.BASIC_ISO_DATE),
        openPrice,
        highPrice,
        lowPrice,
        closePrice,
        volume,
        stock
    );
  }
}
