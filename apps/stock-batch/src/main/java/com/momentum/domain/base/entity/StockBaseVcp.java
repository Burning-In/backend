package com.momentum.domain.base.entity;

import jakarta.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@Embeddable
public class StockBaseVcp {

  private static final int MOVING_AVERAGE_WINDOW = 2;

  private boolean isVcp = false;

  public void updateVcp(List<Long> volatilityHistories) {
    if (volatilityHistories == null || volatilityHistories.size() < 2) {
      return;
    }
    if (volatilityHistories.size() == 2) {
      this.isVcp = volatilityHistories.get(0) > volatilityHistories.get(1);
      return;
    }
    List<Double> movingAverages = calculateMovingAverage(volatilityHistories);
    this.isVcp = calculateMovingAverageTrendSlope(movingAverages) < 0;
  }

  private List<Double> calculateMovingAverage(List<Long> histories) {
    if (histories == null || histories.size() < MOVING_AVERAGE_WINDOW) {
      return List.of();
    }
    List<Double> result = new ArrayList<>();
    long sum = 0;
    for (int i = 0; i < MOVING_AVERAGE_WINDOW; i++) {
      sum += histories.get(i);
    }
    result.add((double) sum / MOVING_AVERAGE_WINDOW);
    for (int i = MOVING_AVERAGE_WINDOW; i < histories.size(); i++) {
      sum += (int) (histories.get(i) - histories.get(i - MOVING_AVERAGE_WINDOW));
      result.add((double) sum / MOVING_AVERAGE_WINDOW);
    }
    return result;
  }

  private double calculateMovingAverageTrendSlope(List<Double> movingAverages) {
    if (movingAverages == null || movingAverages.isEmpty()) {
      return 0;
    }

    int n = movingAverages.size();
    double indexSum = 0;
    double valueSum = 0;
    double indexValueProductSum = 0;
    double indexSquareSum = 0;
    for (int i = 0; i < n; i++) {
      indexSum += i;
      valueSum += movingAverages.get(i);
      indexValueProductSum += (double) i * movingAverages.get(i);
      indexSquareSum += (double) i * i;
    }
    return (n * indexValueProductSum - indexSum * valueSum) / (n * indexSquareSum - indexSum * indexSum);
  }
}
