package com.momentum.domain.entity.indicator.price;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPivotType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class StockPivot extends BaseEntity {

  private long price;

  private LocalDate tradeDate;

  @Enumerated(EnumType.STRING)
  private StockPivotType stockPivotType;

  @ManyToOne
  private Stock stock;

  public StockPivot(long price, LocalDate tradeDate, StockPivotType stockPivotType, Stock stock) {
    this.price = price;
    this.tradeDate = tradeDate;
    this.stockPivotType = stockPivotType;
    this.stock = stock;
  }

  public static StockPivot create(long closingPrice, LocalDate tradeDate, Stock stock) {
    return new StockPivot(closingPrice, tradeDate, StockPivotType.UNDEFINED, stock);
  }

  public void updateType(StockPivotType stockPivotType) {
    if (stockPivotType == null || this.stockPivotType.equals(stockPivotType)) {
      return;
    }
    this.stockPivotType = stockPivotType;
  }
}
