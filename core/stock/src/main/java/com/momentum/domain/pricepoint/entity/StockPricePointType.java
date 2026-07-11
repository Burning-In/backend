package com.momentum.domain.pricepoint.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum StockPricePointType {
  HIGH,
  LOW,
  ASCENDING,
  DESCENDING,
  FLAT,
  UNKNOWN;

  public static StockPricePointType classify(StockPricePoint previous, StockPricePoint target, StockPricePoint next) {
    if (target == null || next == null) {
      return UNKNOWN;
    }
    if (previous == null) {
      if (target.compareTo(next) > 0) {
        return HIGH;
      }
      if (target.compareTo(next) < 0) {
        return LOW;
      }
      return FLAT;
    }
    if (target.compareTo(previous) > 0 && target.compareTo(next) > 0) {
      return HIGH;
    }
    if (target.compareTo(previous) < 0 && target.compareTo(next) < 0) {
      return LOW;
    }
    if (target.compareTo(previous) > 0 && target.compareTo(next) < 0) {
      return ASCENDING;
    }
    if (target.compareTo(previous) < 0 && target.compareTo(next) > 0) {
      return DESCENDING;
    }
    return FLAT;
  }

  public static boolean isNonPivot(StockPricePoint point) {
    return point.getType() == FLAT
        || point.getType() == UNKNOWN
        || point.getType() == ASCENDING
        || point.getType() == DESCENDING;
  }

  public static boolean isFlat(StockPricePoint first, StockPricePoint second, double flatThreshold) {
    if (first == null || second == null) {
      return false;
    }
    BigDecimal p1 = BigDecimal.valueOf(first.getPrice());
    BigDecimal p2 = BigDecimal.valueOf(second.getPrice());
    return p1.subtract(p2)
        .abs()
        .divide(p2, 10, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .compareTo(BigDecimal.valueOf(flatThreshold)) <= 0;
  }
}
