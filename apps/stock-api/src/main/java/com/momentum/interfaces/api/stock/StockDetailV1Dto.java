package com.momentum.interfaces.api.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StockDetailV1Dto {

    // ===================== Stock Summary Section =====================

    public record SummaryResponse(
        String stockCode,
        String stockName,       // 종목 이름
        String sector,          // 섹터
        String status,          // 상태기
        BigDecimal price,       // 가격
        BigDecimal noise        // 기타노이즈
    ) {}

    // ===================== Chart Section =====================

    public record CandleChartResponse(
        List<CandleItem> candles
    ) {
        public record CandleItem(
            LocalDate date,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            Long volume
        ) {}
    }

    // ===================== Core Insight + Technical Insight Section =====================

    public record InsightResponse(
        CoreInsight coreInsight,
        TechnicalInsight technicalInsight
    ) {
        public record CoreInsight(
            String regimeState,       // 레짐 상태 (상/하/중립 등)
            String regimeDescription  // 레짐 설명
        ) {}

        public record TechnicalInsight(
            String movingAverageInfo,   // 이동평균선 정보
            String basePhase,           // 베이스 (phase)
            String resistanceInfo,      // 저항선 정보
            String supportInfo          // 지지선 정보
        ) {}
    }

    // ===================== Rs Section =====================

    public record RsResponse(
        String kospiComparisonInfo  // KOSPI 대비 정보
    ) {}

    // ===================== EPS Section =====================

    public record EpsResponse(
        List<QuarterlyEpsItem> quarterlyEps,  // 분기별 EPS
        String epsEvaluation                  // EPS 평가 정보
    ) {
        public record QuarterlyEpsItem(
            String quarter,
            BigDecimal eps
        ) {}
    }

    // ===================== Expected Return Section =====================

    public record ExpectedReturnResponse(
        BigDecimal expectedReturnValue,    // 기대 수익률 값
        String expectedReturnModel         // 기대 수익률 모델 설명
    ) {}

}
