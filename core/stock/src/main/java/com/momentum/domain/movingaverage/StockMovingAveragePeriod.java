package com.momentum.domain.movingaverage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public enum StockMovingAveragePeriod {

  MA_50(50),
  MA_150(150),
  MA_200(200);

  private final int period;

  StockMovingAveragePeriod(int period) {
    this.period = period;
  }

  public static List<StockMovingAveragePeriod> ascending() {
    return Arrays.stream(values())
        .sorted(Comparator.comparingInt(StockMovingAveragePeriod::getPeriod))
        .toList();
  }

  public static int longestPeriod() {
    return Arrays.stream(values())
        .mapToInt(StockMovingAveragePeriod::getPeriod)
        .max()
        .orElse(0);
  }
}
