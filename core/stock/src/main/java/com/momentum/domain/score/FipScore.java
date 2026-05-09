package com.momentum.domain.score;

import com.momentum.domain.stockcandle.StockDailyCandle;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FipScore {

  private static final int TRADING_DAYS_PER_YEAR = 252;

  private BigDecimal fip;
  private int upDays;
  private int downDays;

  private FipScore(BigDecimal fip, int upDays, int downDays) {
    this.fip = fip;
    this.upDays = upDays;
    this.downDays = downDays;
  }

  public static FipScore calculate(Momentum momentum, List<StockDailyCandle> candles) {
    int upDays = 0;
    int downDays = 0;
    for (int i = 0; i < candles.size() - 1; i++) {
      long change = candles.get(i).getClosePrice() - candles.get(i + 1).getClosePrice();
      if (change > 0) {
        upDays++;
      } else if (change < 0) {
        downDays++;
      }
    }
    BigDecimal fip = computeFip(momentum.getValue(), upDays, downDays);
    return new FipScore(fip, upDays, downDays);
  }

  public static FipScore of(BigDecimal fip, int upDays, int downDays) {
    return new FipScore(fip, upDays, downDays);
  }

  // FIP = Sign(12개월 수익률) × [(상승일수 / 252) - (하락일수 / 252)]
  private static BigDecimal computeFip(BigDecimal momentum, int upDays, int downDays) {
    BigDecimal pctPositive = BigDecimal.valueOf(upDays)
        .divide(BigDecimal.valueOf(TRADING_DAYS_PER_YEAR), 6, RoundingMode.HALF_UP);
    BigDecimal pctNegative = BigDecimal.valueOf(downDays)
        .divide(BigDecimal.valueOf(TRADING_DAYS_PER_YEAR), 6, RoundingMode.HALF_UP);
    return sign(momentum).multiply(pctPositive.subtract(pctNegative));
  }

  private static BigDecimal sign(BigDecimal value) {
    if (value.compareTo(BigDecimal.ZERO) > 0) {
      return BigDecimal.ONE;
    }
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.valueOf(-1);
    }
    return BigDecimal.ZERO;
  }
}
