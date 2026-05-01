package com.momentum.infrastructure.dto;

import java.time.LocalDate;

public record StockCandleRequest(
    String stockCode,
    int count,
    String startDate,
    String endDate
) {

  public static StockCandleRequest of(String stockCode, LocalDate baseDate) {
    return new StockCandleRequest(
        stockCode,
        1,
        baseDate.toString(),
        baseDate.toString()
    );
  }
}
