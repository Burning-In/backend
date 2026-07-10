package com.momentum.domain.base.entity;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_HIGH;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBaseLine extends BaseEntity {

  @Embedded
  private StockBaseLinePrice price;

  @Embedded
  private StockBaseLineStrength strength;

  @Enumerated(EnumType.STRING)
  private StockBaseLineType type;

  @ManyToOne
  private StockBase stockBase;

  private StockBaseLine(StockBaseLinePrice price, StockBaseLineStrength strength,
      StockBaseLineType type, StockBase stockBase) {
    this.price = price;
    this.strength = strength;
    this.type = type;
    this.stockBase = stockBase;
  }

  public static StockBaseLine create(StockPricePoint point, long baseAverageVolume, StockBase stockBase) {
    if (point.isSameType(PIVOT_HIGH)) {
      return StockBaseLine.resistance(point.getPrice(), point.getVolume(), baseAverageVolume, stockBase);
    }
    return StockBaseLine.support(point.getPrice(), point.getVolume(), baseAverageVolume, stockBase);
  }

  private static StockBaseLine resistance(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        new StockBaseLinePrice(closePrice),
        StockBaseLineStrength.create(currentVolume, averageDailyVolume),
        StockBaseLineType.RESISTANCE,
        stockBase
    );
  }

  private static StockBaseLine support(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        new StockBaseLinePrice(closePrice),
        StockBaseLineStrength.create(currentVolume, averageDailyVolume),
        StockBaseLineType.SUPPORT,
        stockBase
    );
  }

  public void updateStrength(Long additionalVolume, Long averageDailyVolume) {
    this.strength.touch(additionalVolume, averageDailyVolume);
  }

  public void convertLineType() {
    if (this.type == StockBaseLineType.RESISTANCE) {
      this.type = StockBaseLineType.SUPPORT;
    } else {
      this.type = StockBaseLineType.RESISTANCE;
    }
  }

  public boolean matches(StockPricePoint point, double threshold) {
    StockBaseLineType expectedType = StockBaseLineType.SUPPORT;
    if (point.isSameType(PIVOT_HIGH)) {
      expectedType = StockBaseLineType.RESISTANCE;
    }
    return this.type == expectedType
        && this.price.isWithinThreshold(point.getPrice(), threshold);
  }

  public boolean isStrongerThan(StockBaseLine other) {
    return this.strength.compareTo(other.getStrength()) > 0;
  }

  public long getPrice() {
    return this.price.getPrice();
  }

  public long getUpperBound(double threshold) {
    return this.price.getUpperBound(threshold);
  }

  public long getLowerBound(double threshold) {
    return this.price.getLowerBound(threshold);
  }
}
