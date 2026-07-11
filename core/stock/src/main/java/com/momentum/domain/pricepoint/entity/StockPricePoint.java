package com.momentum.domain.pricepoint.entity;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
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
  private StockPricePointPrice price;

  @Enumerated(EnumType.STRING)
  private StockPricePointType type;

  @ManyToOne(optional = true)
  private StockBase stockBase;

  @ManyToOne(optional = false)
  private Stock stock;

  public StockPricePoint(long price, long volume, LocalDate tradeDate,
      StockPricePointType type, StockBase stockBase, Stock stock) {
    this.price = new StockPricePointPrice(price);
    this.volume = volume;
    this.tradeDate = Objects.requireNonNull(tradeDate);
    this.type = Objects.requireNonNull(type);
    this.stockBase = stockBase;
    this.stock = Objects.requireNonNull(stock);
  }

  public static StockPricePoint init(long closePrice, long volume, LocalDate tradeDate, Stock stock) {
    return new StockPricePoint(closePrice, volume, tradeDate, StockPricePointType.UNKNOWN, null, stock);
  }

  public void updateType(StockPricePointType stockPricePointType) {
    if (stockPricePointType == null || this.type.equals(stockPricePointType)) {
      return;
    }
    this.type = stockPricePointType;
  }

  public void assignBase(StockBase stockBase) {
    if (stockBase == null || stockBase.equals(this.stockBase)) {
      return;
    }
    this.stockBase = stockBase;
  }

  @Override
  public int compareTo(StockPricePoint other) {
    return this.price.compareTo(other.price);
  }

  public long getPrice() {
    return price.getPrice();
  }

  public boolean isSameType(StockPricePointType stockPricePointType) {
    if (stockPricePointType == null) {
      return false;
    }
    return this.type.equals(stockPricePointType);
  }
}
