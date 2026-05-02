package com.momentum.domain.base.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBaseLinePrice {

  private long price;

  public StockBaseLinePrice(long price) {
    this.price = price;
  }

  public long getUpperBound(double threshold) {
    return (long) (price * (threshold / 100.0 + 1));
  }

  public long getLowerBound(double threshold) {
    return (long) (price * (-threshold / 100.0 + 1));
  }

  public boolean isWithinThreshold(long pointPrice, double threshold) {
    double diff = Math.abs(price - pointPrice) / (double) price;
    return diff <= threshold;
  }
}
