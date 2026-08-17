package com.momentum.domain.anchorpoint.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockAnchorPointCalculation extends BaseEntity {

  private long currentPrice;
  private long volume;
  private LocalDate tradeDate;

  private BigDecimal slopeUpperMax;
  private BigDecimal slopeLowerMin;

  @ManyToOne
  private StockAnchorPoint stockAnchorPoint;

  private StockAnchorPointCalculation(long currentPrice, long volume, LocalDate tradeDate, BigDecimal slopeUpperMax,
      BigDecimal slopeLowerMin, StockAnchorPoint stockAnchorPoint) {
    this.currentPrice = currentPrice;
    this.volume = volume;
    this.tradeDate = Objects.requireNonNull(tradeDate);
    this.slopeUpperMax = Objects.requireNonNull(slopeUpperMax);
    this.slopeLowerMin = Objects.requireNonNull(slopeLowerMin);
    this.stockAnchorPoint = Objects.requireNonNull(stockAnchorPoint);
  }

  public static StockAnchorPointCalculation create(long currentPrice, long volume, LocalDate tradeDate,
      BigDecimal slopeUpperMax, BigDecimal slopeLowerMin, StockAnchorPoint anchorPoint) {
    return new StockAnchorPointCalculation(currentPrice, volume, tradeDate, slopeUpperMax, slopeLowerMin, anchorPoint);
  }
}
