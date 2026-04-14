package com.momentum.domain.entity.indicator.price;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBaseVolatility {

  private Double currentVolatility;
  private Double previousVolatility;
  private Double prePreviousVolatility;

  public StockBaseVolatility(Double currentVolatility, Double previousVolatility, Double prePreviousVolatility) {
    this.currentVolatility = currentVolatility;
    this.previousVolatility = previousVolatility;
    this.prePreviousVolatility = prePreviousVolatility;
  }

  public boolean isContracting() {
    return prePreviousVolatility != null &&
        previousVolatility != null &&
        currentVolatility != null &&
        prePreviousVolatility > previousVolatility &&
        previousVolatility > currentVolatility;
  }



}
