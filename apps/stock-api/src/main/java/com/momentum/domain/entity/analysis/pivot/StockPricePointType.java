package com.momentum.domain.entity.analysis.pivot;

public enum StockPricePointType {
  PIVOT_HIGH,
  PIVOT_LOW,
  ASCENDING,
  DESCENDING,
  FLAT,
  INIT;

  public static StockPricePointType resolve(Long firstPrice, long middlePrice, long lastPrice) {
    if (firstPrice == null) {
      if (middlePrice > lastPrice) {
        return PIVOT_HIGH;
      }
      if (middlePrice < lastPrice) {
        return PIVOT_LOW;
      }
      return FLAT;
    }
    if (middlePrice > firstPrice && middlePrice > lastPrice) {
      return PIVOT_HIGH;
    }
    if (middlePrice < firstPrice && middlePrice < lastPrice) {
      return PIVOT_LOW;
    }
    if (firstPrice < middlePrice && middlePrice < lastPrice) {
      return ASCENDING;
    }
    if (firstPrice > middlePrice && middlePrice > lastPrice) {
      return DESCENDING;
    }
    return FLAT;
  }

  public static boolean isNonPivot(StockPricePoint point) {
    return point.getStockPricePointType() == FLAT || point.getStockPricePointType() == INIT
        || point.getStockPricePointType() == ASCENDING || point.getStockPricePointType() == DESCENDING;
  }
}
