package com.momentum.domain.pricepoint.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class StockPricePointSlopeCalculator {

  public SlopeResult calculateSlope(long pivotPrice, LocalDate pivotDate, long todayPrice, LocalDate todayDate,
      BigDecimal pivotErrorPercent) {
    if (pivotDate == null || todayDate == null) {
      throw new IllegalArgumentException("Pivot date and time cannot be null");
    }
    long daysBetween = ChronoUnit.DAYS.between(pivotDate, todayDate);
    BigDecimal priceDiff = BigDecimal.valueOf(todayPrice - pivotPrice);
    BigDecimal days = BigDecimal.valueOf(daysBetween);

    BigDecimal su = priceDiff.subtract(pivotErrorPercent)
        .divide(days, 10, RoundingMode.HALF_UP);
    BigDecimal sl = priceDiff.add(pivotErrorPercent)
        .divide(days, 10, RoundingMode.HALF_UP);

    return new SlopeResult(su, sl);
  }

  public record SlopeResult(BigDecimal upper, BigDecimal lower) {

  }
}
