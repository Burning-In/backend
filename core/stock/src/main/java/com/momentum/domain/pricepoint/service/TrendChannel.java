package com.momentum.domain.pricepoint.service;

import java.math.BigDecimal;
import java.util.Objects;

public record TrendChannel(BigDecimal slopeUpperMax, BigDecimal slopeLowerMin) {

  public TrendChannel {
    Objects.requireNonNull(slopeUpperMax);
    Objects.requireNonNull(slopeLowerMin);
  }

  public boolean isOutOfChannel() {
    return slopeUpperMax.compareTo(slopeLowerMin) > 0;
  }
}
