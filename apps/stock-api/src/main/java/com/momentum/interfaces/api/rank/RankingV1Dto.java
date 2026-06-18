package com.momentum.interfaces.api.rank;

import java.math.BigDecimal;
import java.util.List;

public class RankingV1Dto {

    // ===================== 돌파 성공 (BREAKOUT_SUCCESS) =====================

    public record BreakoutSuccessResponse(
        List<BreakoutSuccessItem> stocks
    ) {
        public record BreakoutSuccessItem(
            String stockName,
            String stockCode,
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
            String stockCode,
            BigDecimal currentPrice,
            BigDecimal oneYearMomentum,
            BigDecimal fipScore
        ) {}
    }
}
