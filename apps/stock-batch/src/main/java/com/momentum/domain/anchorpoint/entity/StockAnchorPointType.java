package com.momentum.domain.anchorpoint.entity;

public enum StockAnchorPointType {
  HIGH,
  LOW,
  ASCENDING,
  DESCENDING,
  FLAT,
  UNKNOWN;

  public boolean isNonPivot() {
    return this == FLAT || this == UNKNOWN || this == ASCENDING || this == DESCENDING;
  }
}
