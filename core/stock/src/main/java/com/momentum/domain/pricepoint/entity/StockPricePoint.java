package com.momentum.domain.pricepoint.entity;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_LOW;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockDailyCandle;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class StockPricePoint extends BaseEntity implements Comparable<StockPricePoint> {

  private long volume;
  private LocalDate tradeDate;

  @Embedded
  private StockPricePointPrice stockPricePointPrice;

  @Enumerated(EnumType.STRING)
  private StockPricePointType stockPricePointType;

  @ManyToOne(optional = true)
  private StockBase stockBase;

  @ManyToOne(optional = false)
  private Stock stock;

  public StockPricePoint(long price, long volume, LocalDate tradeDate,
      StockPricePointType stockPricePointType, StockBase stockBase, Stock stock) {
    this.stockPricePointPrice = new StockPricePointPrice(price);
    this.volume = volume;
    this.tradeDate = Objects.requireNonNull(tradeDate);
    this.stockPricePointType = Objects.requireNonNull(stockPricePointType);
    this.stockBase = stockBase;
    this.stock = Objects.requireNonNull(stock);
  }

  public static StockPricePoint initialize(StockDailyCandle stockDailyCandle) {
    return new StockPricePoint(
        stockDailyCandle.getClosePrice(),
        stockDailyCandle.getVolume(),
        stockDailyCandle.getTradeDate(),
        StockPricePointType.INIT,
        null,
        stockDailyCandle.getStock()
    );
  }

  public void updateType(StockPricePointType stockPricePointType) {
    if (stockPricePointType == null || this.stockPricePointType.equals(stockPricePointType)) {
      return;
    }
    this.stockPricePointType = stockPricePointType;
  }

  public void assignBase(StockBase stockBase) {
    if (stockBase == null || this.stockBase.equals(stockBase)) {
      return;
    }
    this.stockBase = stockBase;
  }

  public boolean isFlat(StockPricePoint other, BigDecimal flatThreshold) {
    return this.stockPricePointPrice.isFlat(other.stockPricePointPrice, flatThreshold);
  }

  @Override
  public int compareTo(StockPricePoint other) {
    return this.stockPricePointPrice.compareTo(other.stockPricePointPrice);
  }

  public long getPrice() {
    return stockPricePointPrice.getPrice();
  }

  public boolean isSameType(StockPricePointType stockPricePointType) {
    return this.stockPricePointType.equals(stockPricePointType);
  }
}
