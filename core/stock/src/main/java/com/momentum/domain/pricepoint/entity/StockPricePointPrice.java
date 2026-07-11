package com.momentum.domain.pricepoint.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class StockPricePointPrice implements Comparable<StockPricePointPrice> {

  private long price;

  public StockPricePointPrice(long price) {
    this.price = price;
  }

  @Override
  public int compareTo(StockPricePointPrice other) {
    return Long.compare(this.price, other.price);
  }
}
