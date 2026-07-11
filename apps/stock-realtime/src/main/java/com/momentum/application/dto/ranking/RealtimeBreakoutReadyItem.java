package com.momentum.application.dto.ranking;

import com.momentum.domain.score.StockRankScore;
import java.math.BigDecimal;
import java.util.List;

public record RealtimeBreakoutReadyItem(
    String stockName,
    String stockCode,
    BigDecimal currentPrice,
    BigDecimal oneYearMomentum,
    BigDecimal fipScore
) {

  public static List<RealtimeBreakoutReadyItem> from(List<StockRankScore> ranked) {
    return ranked.stream()
        .map(score -> new RealtimeBreakoutReadyItem(
            score.getStock().getName(),
            score.getStock().getCode(),
            null,
            score.getMomentumScore().getValue(),
            score.getFrogInPanScore().getValue()))
        .toList();
  }
}
