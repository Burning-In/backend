package com.momentum.application.dto.ranking;

import com.momentum.domain.score.StockRankScore;
import java.math.BigDecimal;
import java.util.List;

public record RealtimeBreakoutSuccessItem(
    String stockName,
    String stockCode,
    BigDecimal currentPrice,
    BigDecimal oneYearMomentum,
    BigDecimal frogInPanScore
) {

  public static List<RealtimeBreakoutSuccessItem> from(List<StockRankScore> ranked) {
    return ranked.stream()
        .map(score -> new RealtimeBreakoutSuccessItem(
            score.getStock().getCode(),
            score.getStock().getName(),
            null,
            score.getMomentumScore().getValue(),
            score.getFrogInPanScore().getValue()))
        .toList();
  }
}
