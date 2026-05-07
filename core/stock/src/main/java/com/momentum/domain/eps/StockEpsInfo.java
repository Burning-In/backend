package com.momentum.domain.eps;

import com.momentum.domain.stock.Stock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public record StockEpsInfo(
    Stock stock,
    YearMonth quarterlyDate,
    double eps
) {

  private static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

  public static StockEpsInfo of(Stock stock, String date, String eps) {
    return new StockEpsInfo(
        stock,
        YearMonth.parse(date, YM_FORMATTER),
        Double.parseDouble(eps)
    );
  }
}
