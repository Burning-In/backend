package com.momentum.domain.entity.indicator.price;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPivotCalculateHistory extends BaseEntity {

  private long currentPrice;
  private BigDecimal SU_MAX;
  private BigDecimal SL_MIN;
  @ManyToOne
  private StockPivot stockPivot;

  private StockPivotCalculateHistory(long currentPrice, BigDecimal SU_MAX, BigDecimal SL_MIN,
      StockPivot stockPivot) {
    this.currentPrice = currentPrice;
    this.SU_MAX = Objects.requireNonNull(SU_MAX);
    this.SL_MIN = Objects.requireNonNull(SL_MIN);
    this.stockPivot = Objects.requireNonNull(stockPivot);
  }

  public static StockPivotCalculateHistory create(
      long currentPrice, BigDecimal SU_MAX, BigDecimal SL_MIN, StockPivot stockPivot
  ) {
    return new StockPivotCalculateHistory(
        currentPrice, SU_MAX, SL_MIN, stockPivot
    );
  }
}
