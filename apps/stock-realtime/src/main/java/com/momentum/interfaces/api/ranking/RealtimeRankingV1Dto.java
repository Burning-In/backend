package com.momentum.interfaces.api.ranking;

import java.math.BigDecimal;
import java.util.List;

public class RealtimeRankingV1Dto {

    // ===================== 돌파시작 (BREAKOUT_START) =====================

    public record RealtimeBreakoutStartResponse(
        List<RealtimeBreakoutStartItem> stocks
    ) {
        public record RealtimeBreakoutStartItem(
            String stockName,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }

    // ===================== 돌파준비 (BREAKOUT_READY) =====================

    public record RealtimeBreakoutReadyResponse(
        List<RealtimeBreakoutReadyItem> stocks
    ) {
        public record RealtimeBreakoutReadyItem(
            String stockName,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }

    // ===================== 돌파실패 (BREAKOUT_FAILED) =====================

    public record RealtimeBreakoutFailedResponse(
        List<RealtimeBreakoutFailedItem> stocks
    ) {
        public record RealtimeBreakoutFailedItem(
            String stockName,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }
}
