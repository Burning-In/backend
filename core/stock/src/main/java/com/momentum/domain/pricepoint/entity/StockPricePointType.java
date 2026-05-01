package com.momentum.domain.pricepoint.entity;

public enum StockPricePointType {
  PIVOT_HIGH,
  PIVOT_LOW,
  ASCENDING,
  DESCENDING,
  FLAT,
  INIT;

  public static StockPricePointType resolve(StockPricePoint first, StockPricePoint middle, StockPricePoint last) {
    if (first == null) {
      if (middle.compareTo(last) > 0) {
        return PIVOT_HIGH;
      }
      if (middle.compareTo(last) < 0) {
        return PIVOT_LOW;
      }
      return FLAT;
    }
    if (middle.compareTo(first) > 0 && middle.compareTo(last) > 0) {
      return PIVOT_HIGH;
    }
    if (middle.compareTo(first) < 0 && middle.compareTo(last) < 0) {
      return PIVOT_LOW;
    }
    if (first.compareTo(middle) < 0 && middle.compareTo(last) < 0) {
      return ASCENDING;
    }
    if (first.compareTo(middle) > 0 && middle.compareTo(last) > 0) {
      return DESCENDING;
    }
    return FLAT;
  }

  public static boolean isNonPivot(StockPricePoint point) {
    return point.getStockPricePointType() == FLAT
        || point.getStockPricePointType() == INIT
        || point.getStockPricePointType() == ASCENDING
        || point.getStockPricePointType() == DESCENDING;
  }
}
