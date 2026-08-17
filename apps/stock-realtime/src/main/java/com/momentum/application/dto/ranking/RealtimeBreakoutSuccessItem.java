package com.momentum.application.dto.ranking;

import com.momentum.infrastructure.query.RankedStockRow;
import java.math.BigDecimal;
import java.util.List;

public record RealtimeBreakoutSuccessItem(
    String stockName,
    String stockCode,
    BigDecimal currentPrice,
    BigDecimal oneYearMomentum,
    BigDecimal frogInPanScore
) {

  public static List<RealtimeBreakoutSuccessItem> from(List<RankedStockRow> ranked) {
    return ranked.stream()
        .map(row -> new RealtimeBreakoutSuccessItem(
            row.stockName(),
            row.stockCode(),
            null,
            row.momentum(),
            row.fip()))
        .toList();
  }
}
