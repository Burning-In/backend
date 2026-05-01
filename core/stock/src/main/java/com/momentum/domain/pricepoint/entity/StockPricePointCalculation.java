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
public class StockPricePointCalculation extends BaseEntity {

  private long currentPrice;
  private BigDecimal slopeUpperMax;
  private BigDecimal slopeLowerMin;
  @ManyToOne
  private StockPricePoint stockPricePoint;

  private StockPricePointCalculation(long currentPrice, BigDecimal slopeUpperMax, BigDecimal slopeLowerMin,
      StockPricePoint stockPricePoint) {
    this.currentPrice = currentPrice;
    this.slopeUpperMax = Objects.requireNonNull(slopeUpperMax);
    this.slopeLowerMin = Objects.requireNonNull(slopeLowerMin);
    this.stockPricePoint = Objects.requireNonNull(stockPricePoint);
  }

  public static StockPricePointCalculation create(
      long closingPrice, BigDecimal SU_MAX, BigDecimal slopeLowerMin, StockPricePoint anchorPoint
  ) {
    return new StockPricePointCalculation(closingPrice, SU_MAX, slopeLowerMin, anchorPoint);
  }
}
