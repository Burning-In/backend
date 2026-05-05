package com.momentum.interfaces.api.rank;

import java.math.BigDecimal;
import java.util.List;

public class RankingV1Dto {

    // ===================== 돌파시작 (BREAKOUT_START) =====================

    public record BreakoutStartResponse(
        List<BreakoutStartItem> stocks
    ) {
        public record BreakoutStartItem(
            String stockCode,
            String stockName,
            BigDecimal resistancePrice,
            BigDecimal currentPrice,
            BigDecimal changeRateSinceBreakout
        ) {}
    }

    // ===================== 돌파준비 (BREAKOUT_READY) =====================

    public record BreakoutReadyResponse(
        List<BreakoutReadyItem> stocks
    ) {
        public record BreakoutReadyItem(
            String stockCode,
            String stockName,
            BigDecimal supportPrice,
            BigDecimal resistancePrice,
            BigDecimal currentPrice,
            BigDecimal changeRateToResistance
        ) {}
    }

    // ===================== 돌파실패 (BREAKOUT_FAILED) =====================

    public record BreakoutFailedResponse(
        List<BreakoutFailedItem> stocks
    ) {
        public record BreakoutFailedItem(
            String stockCode,
            String stockName,
            BigDecimal resistancePrice,
            BigDecimal currentPrice,
            BigDecimal changeRateSinceBreakoutFailure
        ) {}
    }
}
