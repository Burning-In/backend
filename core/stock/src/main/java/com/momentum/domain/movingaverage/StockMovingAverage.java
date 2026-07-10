package com.momentum.domain.movingaverage;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class StockMovingAverage extends BaseEntity {

  private long ma;
  @Enumerated(EnumType.STRING)
  private StockMovingAveragePeriod stockMovingAveragePeriod;
  @ManyToOne
  private Stock stock;

  public StockMovingAverage(long ma, StockMovingAveragePeriod stockMovingAveragePeriod, Stock stock) {
    this.ma = ma;
    this.stockMovingAveragePeriod = stockMovingAveragePeriod;
    this.stock = stock;
  }
}
