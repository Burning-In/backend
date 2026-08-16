package com.momentum.domain.score;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FrogInPanScore {

  private static final int TRADING_DAYS_PER_YEAR = 252;

  private BigDecimal value;
  private int upDays;
  private int downDays;

  private FrogInPanScore(BigDecimal value, int upDays, int downDays) {
    this.value = Objects.requireNonNull(value);
    this.upDays = upDays;
    this.downDays = downDays;
  }

  public static FrogInPanScore calculate(BigDecimal momentumValue, List<Long> closePrices) {
    if (closePrices == null || closePrices.isEmpty()) {
      return new FrogInPanScore(momentumValue, 0, 0);
    }
    int upDays = 0;
    int downDays = 0;
    for (int i = 0; i < closePrices.size() - 1; i++) {
      long change = closePrices.get(i) - closePrices.get(i + 1);
      if (change > 0) {
        upDays++;
      } else if (change < 0) {
        downDays++;
      }
    }
    BigDecimal fip = computeFip(momentumValue, upDays, downDays);
    return new FrogInPanScore(fip, upDays, downDays);
  }

  // FIP = Sign(12개월 수익률) × [(상승일수 / 252) - (하락일수 / 252)]
  private static BigDecimal computeFip(BigDecimal momentumValue, int upDays, int downDays) {
    if(momentumValue == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal pctPositive = BigDecimal.valueOf(upDays)
        .divide(BigDecimal.valueOf(TRADING_DAYS_PER_YEAR), 6, RoundingMode.HALF_UP);
    BigDecimal pctNegative = BigDecimal.valueOf(downDays)
        .divide(BigDecimal.valueOf(TRADING_DAYS_PER_YEAR), 6, RoundingMode.HALF_UP);
    return sign(momentumValue).multiply(pctPositive.subtract(pctNegative));
  }

  private static BigDecimal sign(BigDecimal momentumValue) {
    if(momentumValue == null) {
      return BigDecimal.ZERO;
    }
    if (momentumValue.compareTo(BigDecimal.ZERO) > 0) {
      return BigDecimal.ONE;
    }
    if (momentumValue.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.valueOf(-1);
    }
    return BigDecimal.ZERO;
  }
}
