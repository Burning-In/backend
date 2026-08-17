package com.momentum.infrastructure.query;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InsightRows {

  private InsightRows() {
  }

  public record RegimeRow(String stockRegime, long currentPrice, Long supportPrice, Long resistancePrice) {

  }

  public record VolumeRow(long currentVolume, LocalDate baselineFrom) {

  }

  public record RankScoreRow(LocalDate baseDate, BigDecimal momentum, BigDecimal fip, int upDays, int downDays) {

  }

  public record CandlePriceRow(LocalDate tradeDate, long closePrice) {

  }

  public record EpsRow(LocalDate quarter, double eps, Double yearOverYearChangeRate) {

  }
}
