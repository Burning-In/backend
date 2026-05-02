package com.momentum.infrastructure.pricepoint.dto;

import com.momentum.domain.pricepoint.entity.StockPricePoint;

public record RecentPricePoints(
    StockPricePoint point0, // null 가능, 가장 오래된 점
    StockPricePoint point1,
    StockPricePoint point2,
    StockPricePoint point3  // 가장 최신 점
) {

  public boolean hasPoint0() {
    return point0 != null;
  }
}
