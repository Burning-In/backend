package com.momentum.domain.eps;

import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public record StockEpsInfo(
    Stock stock,
    LocalDate quarter,
    double eps
) {

  private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

  public static StockEpsInfo of(Stock stock, String date, String eps) {
    return new StockEpsInfo(
        stock,
        YearMonth.parse(date, YEAR_MONTH_FORMATTER).atDay(1),
        Double.parseDouble(eps)
    );
  }
}
