package com.momentum.domain.anchorpoint.entity;

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
public class StockAnchorPoint extends BaseEntity implements Comparable<StockAnchorPoint> {

  private long volume;
  private LocalDate tradeDate;

  @Embedded
  private StockAnchorPointPrice price;

  @Enumerated(EnumType.STRING)
  private StockAnchorPointType type;

  @ManyToOne(optional = true)
  private StockBase stockBase;

  @ManyToOne(optional = false)
  private Stock stock;

  public StockAnchorPoint(long price, long volume, LocalDate tradeDate,
      StockAnchorPointType type, StockBase stockBase, Stock stock) {
    this.price = new StockAnchorPointPrice(price);
    this.volume = volume;
    this.tradeDate = Objects.requireNonNull(tradeDate);
    this.type = Objects.requireNonNull(type);
    this.stockBase = stockBase;
    this.stock = Objects.requireNonNull(stock);
  }

  public static StockAnchorPoint create(long closePrice, long volume, LocalDate tradeDate, Stock stock) {
    return new StockAnchorPoint(closePrice, volume, tradeDate, StockAnchorPointType.UNKNOWN, null, stock);
  }

  public void updateType(StockAnchorPointType stockAnchorPointType) {
    if (stockAnchorPointType == null || this.type.equals(stockAnchorPointType)) {
      return;
    }
    this.type = stockAnchorPointType;
  }

  public void assignBase(StockBase stockBase) {
    if (stockBase == null || stockBase.equals(this.stockBase)) {
      return;
    }
    this.stockBase = stockBase;
  }

  @Override
  public int compareTo(StockAnchorPoint other) {
    return this.price.compareTo(other.price);
  }

  public long getPrice() {
    return price.getPrice();
  }

  public boolean isSameType(StockAnchorPointType stockAnchorPointType) {
    if (stockAnchorPointType == null) {
      return false;
    }
    return this.type.equals(stockAnchorPointType);
  }
}
