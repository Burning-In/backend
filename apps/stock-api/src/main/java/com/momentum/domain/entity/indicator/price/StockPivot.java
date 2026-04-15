package com.momentum.domain.entity.indicator.price;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
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
public class StockPivot extends BaseEntity {

  private long price;
  private long volume;

  private LocalDate tradeDate;

  @Enumerated(EnumType.STRING)
  private StockPivotType stockPivotType;

  @ManyToOne
  private Stock stock;

  public StockPivot(long price, long volume, LocalDate tradeDate, StockPivotType stockPivotType, Stock stock) {
    this.price = price;
    this.volume = volume;
    this.tradeDate = Objects.requireNonNull(tradeDate);
    this.stockPivotType = Objects.requireNonNull(stockPivotType);
    this.stock = Objects.requireNonNull(stock);
  }

  public static StockPivot create(long closingPrice, long volume, LocalDate tradeDate, Stock stock) {
    return new StockPivot(closingPrice, volume, tradeDate, StockPivotType.UNDEFINED, stock);
  }

  public void updateType(StockPivotType stockPivotType) {
    if (stockPivotType == null || this.stockPivotType.equals(stockPivotType)) {
      return;
    }
    this.stockPivotType = stockPivotType;
  }
}
