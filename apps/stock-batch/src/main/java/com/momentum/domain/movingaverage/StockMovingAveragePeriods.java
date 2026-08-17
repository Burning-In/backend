package com.momentum.domain.movingaverage;

import com.momentum.sharedkernel.StockMovingAveragePeriod;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class StockMovingAveragePeriods {

  private StockMovingAveragePeriods() {
  }

  public static List<StockMovingAveragePeriod> ascending() {
    return Arrays.stream(StockMovingAveragePeriod.values())
        .sorted(Comparator.comparingInt(StockMovingAveragePeriod::getPeriod))
        .toList();
  }

  public static int longest() {
    return Arrays.stream(StockMovingAveragePeriod.values())
        .mapToInt(StockMovingAveragePeriod::getPeriod)
        .max()
        .orElse(0);
  }
}
