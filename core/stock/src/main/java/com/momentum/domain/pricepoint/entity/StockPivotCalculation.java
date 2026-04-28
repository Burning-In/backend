package com.momentum.domain.pricepoint.entity;

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
public class StockPivotCalculation extends BaseEntity {

  private long currentPrice;
  private BigDecimal SU_MAX;
  private BigDecimal SL_MIN;
  @ManyToOne
  private StockPricePoint stockPricePoint;

  private StockPivotCalculation(long currentPrice, BigDecimal SU_MAX, BigDecimal SL_MIN,
      StockPricePoint stockPricePoint) {
    this.currentPrice = currentPrice;
    this.SU_MAX = Objects.requireNonNull(SU_MAX);
    this.SL_MIN = Objects.requireNonNull(SL_MIN);
    this.stockPricePoint = Objects.requireNonNull(stockPricePoint);
  }

  public static StockPivotCalculation create(
      long currentPrice, BigDecimal SU_MAX, BigDecimal SL_MIN, StockPricePoint stockPricePoint
  ) {
    return new StockPivotCalculation(
        currentPrice, SU_MAX, SL_MIN, stockPricePoint
    );
  }
}
