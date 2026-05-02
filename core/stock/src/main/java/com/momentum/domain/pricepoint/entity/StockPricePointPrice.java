package com.momentum.domain.pricepoint.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

  public boolean isFlat(StockPricePointPrice other, BigDecimal flatThreshold) {
    BigDecimal p1 = BigDecimal.valueOf(this.price);
    BigDecimal p2 = BigDecimal.valueOf(other.price);
    BigDecimal diff = p1.subtract(p2).abs()
        .divide(p2, 10, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
    return diff.compareTo(flatThreshold) <= 0;
  }

  @Override
  public int compareTo(StockPricePointPrice other) {
    return Long.compare(this.price, other.price);
  }
}
