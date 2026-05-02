package com.momentum.domain.base.entity;

import jakarta.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@Embeddable
public class StockBaseVcp {

  private boolean isVcp = false;

  public void updateVcp(List<Long> volatilityHistories) {
    if (volatilityHistories.size() == 1) {
      this.isVcp = false;
    } else if (volatilityHistories.size() == 2) {
      this.isVcp = volatilityHistories.get(0) > volatilityHistories.get(1);
    } else {
      this.isVcp = calculateSlope(movingAverage(volatilityHistories)) < 0;
    }
  }

  private List<Double> movingAverage(List<Long> histories) {
    List<Double> result = new ArrayList<>();
    for (int i = 0; i < histories.size() - 1; i++) {
      result.add((histories.get(i) + histories.get(i + 1)) / 2.0);
    }
    return result;
  }

  private double calculateSlope(List<Double> values) {
    int n = values.size();
    double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
    for (int i = 0; i < n; i++) {
      sumX += i;
      sumY += values.get(i);
      sumXY += (double) i * values.get(i);
      sumX2 += (double) i * i;
    }
    return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
  }
}
