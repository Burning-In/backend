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
  private StockBaseLineStrength stockBaseLineStrength;

  @Enumerated(EnumType.STRING)
  private StockBaseLineType lineType;

  @ManyToOne
  private StockBase stockBase;

  private StockBaseLine(StockBaseLinePrice price, StockBaseLineStrength stockBaseLineStrength,
      StockBaseLineType lineType, StockBase stockBase) {
    this.price = price;
    this.stockBaseLineStrength = stockBaseLineStrength;
    this.lineType = lineType;
    this.stockBase = stockBase;
  }

  public static StockBaseLine createLineByPointType(StockPricePoint point, long baseAverageVolume, StockBase stockBase) {
    if (point.isSameType(PIVOT_HIGH)) {
      return StockBaseLine.resistance(point.getPrice(), point.getVolume(), baseAverageVolume, stockBase);
    }
    return StockBaseLine.support(point.getPrice(), point.getVolume(), baseAverageVolume, stockBase);
  }

  public static StockBaseLine resistance(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        new StockBaseLinePrice(closePrice),
        StockBaseLineStrength.create(currentVolume, averageDailyVolume),
        StockBaseLineType.RESISTANCE,
        stockBase
    );
  }

  public static StockBaseLine support(long closePrice, Long currentVolume, Long averageDailyVolume, StockBase stockBase) {
    return new StockBaseLine(
        new StockBaseLinePrice(closePrice),
        StockBaseLineStrength.create(currentVolume, averageDailyVolume),
        StockBaseLineType.SUPPORT,
        stockBase
    );
  }

  public void updateStrength(Long additionalVolume, Long averageDailyVolume) {
    this.stockBaseLineStrength.touch(additionalVolume, averageDailyVolume);
  }

  public void convertLineType() {
    if (this.lineType == StockBaseLineType.RESISTANCE) {
      this.lineType = StockBaseLineType.SUPPORT;
    } else {
      this.lineType = StockBaseLineType.RESISTANCE;
    }
  }

  public boolean isMatched(StockPricePoint point, double threshold) {
    return this.lineType.equals(toLineType(point))
        && this.price.isWithinThreshold(point.getPrice(), threshold);
  }

  public boolean isStrongerThan(StockBaseLine other) {
    return this.stockBaseLineStrength.getStrength()
        .compareTo(other.getStockBaseLineStrength().getStrength()) > 0;
  }

  private StockBaseLineType toLineType(StockPricePoint point) {
    if (point.isSameType(PIVOT_HIGH)) {
      return StockBaseLineType.RESISTANCE;
    }
    return StockBaseLineType.SUPPORT;
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
