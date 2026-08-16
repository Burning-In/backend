package com.momentum.domain.anchorpoint.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class StockAnchorPointSlopeCalculator {

  private static final int SLOPE_SCALE = 10;
  private static final BigDecimal PERCENT_DIVISOR = BigDecimal.valueOf(100);

  public SlopeResult calculateSlope(long anchorPrice, LocalDate anchorDate, long todayPrice, LocalDate todayDate,
      BigDecimal anchorErrorPercent) {
    if (anchorDate == null || todayDate == null) {
      throw new IllegalArgumentException("anchorDate와 todayDate는 null일 수 없다");
    }
    BigDecimal priceDiff = BigDecimal.valueOf(todayPrice - anchorPrice);
    BigDecimal errorPrice = calculateErrorPrice(anchorPrice, anchorErrorPercent);
    BigDecimal days = BigDecimal.valueOf(ChronoUnit.DAYS.between(anchorDate, todayDate));

    return new SlopeResult(
        priceDiff.subtract(errorPrice).divide(days, SLOPE_SCALE, RoundingMode.HALF_UP),
        priceDiff.add(errorPrice).divide(days, SLOPE_SCALE, RoundingMode.HALF_UP)
    );
  }

  private BigDecimal calculateErrorPrice(long anchorPrice, BigDecimal anchorErrorPercent) {
    return BigDecimal.valueOf(anchorPrice)
        .multiply(anchorErrorPercent)
        .divide(PERCENT_DIVISOR, SLOPE_SCALE, RoundingMode.HALF_UP);
  }
}
