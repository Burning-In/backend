package com.momentum.application.dto.ranking;

import com.momentum.domain.score.StockRankScore;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record RealtimeBreakoutSuccessItem(
    String stockName,
    String stockCode,
    BigDecimal currentPrice,
    BigDecimal oneYearMomentum,
    BigDecimal fipScore
) {

  public static List<RealtimeBreakoutSuccessItem> from(List<StockRankScore> ranked) {
    return ranked.stream()
        .map(score -> new RealtimeBreakoutSuccessItem(
            score.getStock().getName(),
            score.getStock().getCode(),
            null,
            score.getMomentum().getValue(),
            score.getFipScore().getFip()))
        .toList();
  }
}
