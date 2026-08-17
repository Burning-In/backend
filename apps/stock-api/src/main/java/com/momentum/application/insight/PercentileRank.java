package com.momentum.application.insight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class PercentileRank {

  private static final BigDecimal RATIO_TO_PERCENT = BigDecimal.valueOf(100);

  private PercentileRank() {
  }

  static BigDecimal of(BigDecimal myScore, List<BigDecimal> peerScores) {
    if (peerScores.isEmpty()) {
      return null;
    }
    long below = peerScores.stream()
        .filter(peerScore -> peerScore.compareTo(myScore) < 0)
        .count();
    return BigDecimal.valueOf(below)
        .multiply(RATIO_TO_PERCENT)
        .divide(BigDecimal.valueOf(peerScores.size()), 1, RoundingMode.HALF_UP);
  }
}
