package com.momentum.application.dto.ranking;

import com.momentum.infrastructure.query.RankedStockRow;
import java.math.BigDecimal;
import java.util.List;

public record RealtimeBreakoutReadyItem(
    String stockName,
    String stockCode,
    BigDecimal currentPrice,
    BigDecimal oneYearMomentum,
    BigDecimal fipScore
) {

  public static List<RealtimeBreakoutReadyItem> from(List<RankedStockRow> ranked) {
    return ranked.stream()
        .map(row -> new RealtimeBreakoutReadyItem(
            row.stockName(),
            row.stockCode(),
            null,
            row.momentum(),
            row.fip()))
        .toList();
  }
}
