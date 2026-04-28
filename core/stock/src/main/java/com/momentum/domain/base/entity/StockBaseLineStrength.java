package com.momentum.domain.base.entity;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBaseLineStrength {

  private Long touchCount;
  private Long accumulatedVolume;
  private BigDecimal strength;

  private StockBaseLineStrength(Long touchCount, Long accumulatedVolume, BigDecimal strength) {
    this.touchCount = touchCount;
    this.accumulatedVolume = accumulatedVolume;
    this.strength = strength;
  }

  public static StockBaseLineStrength create(Long currentVolume, Long averageDailyVolume) {
    return new StockBaseLineStrength(0L, currentVolume, calculateStrength(currentVolume, averageDailyVolume, 0L));
  }

  public void touch(Long additionalVolume, Long averageDailyVolume) {
    this.touchCount++;
    this.accumulatedVolume += additionalVolume;
    this.strength = calculateStrength(this.accumulatedVolume, averageDailyVolume, touchCount);
  }

  private static BigDecimal calculateStrength(Long currentVolume, Long averageDailyVolume, Long touchCount) {
    BigDecimal normalizedVolume = BigDecimal.valueOf(currentVolume)
        .divide(BigDecimal.valueOf(averageDailyVolume), 10, RoundingMode.HALF_UP);
    double log = Math.log(touchCount + 1);

    return normalizedVolume.multiply(BigDecimal.valueOf(log))
        .setScale(4, RoundingMode.HALF_UP);
  }
}
