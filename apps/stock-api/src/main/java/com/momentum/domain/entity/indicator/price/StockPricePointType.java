package com.momentum.domain.entity.indicator.price;

public enum StockPricePointType {
  PIVOT_HIGH,
  PIVOT_LOW,
  FLAT,
  UNDEFINED;

  public static boolean isNonPivot(StockPricePoint point) {
    return point.getStockPricePointType() == FLAT || point.getStockPricePointType() == UNDEFINED;
  }
}
