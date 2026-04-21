package com.momentum.domain.entity.analysis.pivot;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.analysis.base.StockBase;
import com.momentum.domain.entity.stock.Stock;
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
public class StockPricePoint extends BaseEntity {

  private long price;
  private long volume;

  private LocalDate tradeDate;

  @Enumerated(EnumType.STRING)
  private StockPricePointType stockPricePointType;

  @ManyToOne(optional = true)
  private StockBase stockBase;

  @ManyToOne(optional = false)
  private Stock stock;

  public StockPricePoint(long price, long volume, LocalDate tradeDate,
      StockPricePointType stockPricePointType, StockBase stockBase, Stock stock) {
    this.price = price;
    this.volume = volume;
    this.tradeDate = Objects.requireNonNull(tradeDate);
    this.stockPricePointType = Objects.requireNonNull(stockPricePointType);
    this.stockBase = stockBase;
    this.stock = Objects.requireNonNull(stock);
  }

  public static StockPricePoint create(long closingPrice, long volume, LocalDate tradeDate, Stock stock) {
    return new StockPricePoint(closingPrice, volume, tradeDate, StockPricePointType.INIT, null, stock);
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
}
