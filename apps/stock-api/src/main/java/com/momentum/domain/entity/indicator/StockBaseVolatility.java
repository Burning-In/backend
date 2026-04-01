package com.momentum.domain.entity.indicator;

import jakarta.persistence.Embeddable;

@Embeddable
public class StockBaseVolatility {
  private Double currentVolatility;
  private Double previousVolatility;
  private Double prePreviousVolatility;

  public boolean isContracting() {
    return prePreviousVolatility != null &&
        previousVolatility != null &&
        currentVolatility != null &&
        prePreviousVolatility > previousVolatility &&
        previousVolatility > currentVolatility;
  }
}
