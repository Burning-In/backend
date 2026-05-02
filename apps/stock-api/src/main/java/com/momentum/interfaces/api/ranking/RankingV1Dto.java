package com.momentum.interfaces.api.ranking;

import java.math.BigDecimal;
import java.util.List;

public class RankingV1Dto {

    // ===================== 상승시작 테이블 =====================

    public record RiseStartResponse(
        List<RiseStartItem> stocks
    ) {
        public record RiseStartItem(
            String stockCode,
            String stockName,
            BigDecimal resistancePrice,   // 저항선 가격
            BigDecimal currentPrice,      // 현재 가격
            BigDecimal afterBreakout      // 돌파 이후
        ) {}
    }

    // ===================== 상승준비 테이블 =====================

    public record RiseReadyResponse(
        List<RiseReadyItem> stocks
    ) {
        public record RiseReadyItem(
            String stockCode,
            String stockName,
            BigDecimal supportPrice,      // 지지선 가격
            BigDecimal resistancePrice,   // 저항선 가격
            BigDecimal currentPrice,      // 현재 가격
            BigDecimal riseReadiness      // 상승준비도
        ) {}
    }

    // ===================== 하락시작 테이블 =====================

    public record FallStartResponse(
        List<FallStartItem> stocks
    ) {
        public record FallStartItem(
            String stockCode,
            String stockName,
            BigDecimal resistancePrice,       // 저항선 가격
            BigDecimal currentPrice,          // 현재 가격
            BigDecimal afterBreakoutFailure   // 돌파 실패 이후
        ) {}
    }

    // ===================== 하락준비 테이블 =====================

    public record FallReadyResponse(
        List<FallReadyItem> stocks
    ) {
        public record FallReadyItem(
            String stockCode,
            String stockName,
            BigDecimal supportLinePrice,  // 저지선 가격
            BigDecimal currentPrice,      // 현재 가격
            BigDecimal afterCollapse      // 붕괴 이후
        ) {}
    }

}
