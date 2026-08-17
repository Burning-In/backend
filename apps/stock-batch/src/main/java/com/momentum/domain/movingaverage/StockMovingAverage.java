package com.momentum.domain.movingaverage;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockMovingAveragePeriod;
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
public class StockMovingAverage extends BaseEntity {

  private long ma;
  private LocalDate baseDate;
  @Enumerated(EnumType.STRING)
  private StockMovingAveragePeriod stockMovingAveragePeriod;
  @ManyToOne
  private Stock stock;

  public StockMovingAverage(long ma, LocalDate baseDate, StockMovingAveragePeriod stockMovingAveragePeriod,
      Stock stock) {
    this.ma = ma;
    this.baseDate = Objects.requireNonNull(baseDate);
    this.stockMovingAveragePeriod = Objects.requireNonNull(stockMovingAveragePeriod);
    this.stock = Objects.requireNonNull(stock);
  }
}
