package com.momentum.domain.anchorpoint.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class StockAnchorPointPrice implements Comparable<StockAnchorPointPrice> {

  private long price;

  public StockAnchorPointPrice(long price) {
    this.price = price;
  }

  @Override
  public int compareTo(StockAnchorPointPrice other) {
    return Long.compare(this.price, other.price);
  }
}
