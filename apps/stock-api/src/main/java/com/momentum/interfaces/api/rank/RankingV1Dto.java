package com.momentum.interfaces.api.rank;

import java.math.BigDecimal;
import java.util.List;

public class RankingV1Dto {

    // ===================== 돌파시작 (BREAKOUT_START) =====================

    public record BreakoutStartResponse(
        List<BreakoutStartItem> stocks
    ) {
        public record BreakoutStartItem(
            String stockName,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }

    // ===================== 돌파준비 (BREAKOUT_READY) =====================

    public record BreakoutReadyResponse(
        List<BreakoutReadyItem> stocks
    ) {
        public record BreakoutReadyItem(
            String stockName,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }

    // ===================== 돌파실패 (BREAKOUT_FAILED) =====================

    public record BreakoutFailedResponse(
        List<BreakoutFailedItem> stocks
    ) {
        public record BreakoutFailedItem(
            String stockName,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }
}
