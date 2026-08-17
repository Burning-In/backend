package com.momentum.infrastructure.query;

public record RealtimeRegimeRow(
    String stockRegime,
    String stockTrend,
    Long supportPrice,
    Long resistancePrice,
    boolean vcp,
    Long lastAnchorPointPrice
) {

  public boolean hasNoBase() {
    return supportPrice == null || resistancePrice == null;
  }

  public boolean hasNoAnchorPoint() {
    return lastAnchorPointPrice == null;
  }
}
