package com.momentum.interfaces.api.snapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SnapshotV1Dto {

    // ===================== Snapshot List Page =====================

    public record SnapshotListResponse(
        int ongoingSnapshotCount,
        int completedSnapshotCount,
        List<SnapshotListItem> snapshots
    ) {
        public record SnapshotListItem(
            Long snapshotId,
            String stockName,
            boolean isOngoing,
            List<String> insights,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal priceChangeRate
        ) {}
    }

    // ===================== Snapshot Editor Overlay =====================

    // 스냅샷 생성 요청 (시작)
    public record SnapshotCreateRequest(
        String stockCode,
        // Chart toggles
        boolean showMovingAverage,        // 이동평균선 on/off
        boolean showSupportResistance,    // 지지/저항선 on/off
        boolean showMomentumStatus,       // 모멘텀 상태 on/off
        // Core data toggles
        boolean includeExpectedReturn,    // 기대수익률 on/off
        boolean includeRs,                // RS on/off
        boolean includeEps,               // EPS on/off
        // Custom insight
        String memo,                      // 메모
        BigDecimal quantity,              // 수량
        BigDecimal capitalRatio           // 자본%
    ) {}

    public record SnapshotCreateResponse(
        Long snapshotId,
        LocalDate startDate
    ) {}

    // 스냅샷 완료 요청 (완)
    public record SnapshotCompleteRequest(
        // Chart toggles
        boolean showMovingAverage,
        boolean showSupportResistance,
        boolean showMomentumStatus,
        // Core data toggles
        boolean includeExpectedReturn,
        boolean includeRs,
        boolean includeEps,
        // Custom insight
        String memo,
        BigDecimal quantity,
        BigDecimal capitalRatio
    ) {}

    // ===================== 공통 스냅샷 기록 (시작/현재/종료 시점) =====================

    public record SnapshotRecord(
        BigDecimal price,              // 해당 시점 가격
        BigDecimal expectedReturn,     // 기대수익률
        BigDecimal rs,                 // RS
        BigDecimal eps,                // EPS
        BigDecimal capitalRatio,       // 수량/자본%
        String memo                    // 메모
    ) {}

    // ===================== Snapshot Ongoing Detail Page =====================

    public record OngoingDetailResponse(
        Long snapshotId,
        String stockName,              // 종목 이름
        LocalDate startDate,           // 시작일
        SnapshotRecord startRecord,    // 시작 시점 기록
        SnapshotRecord nowRecord       // 현재 시점 기록
    ) {}

    // ===================== Snapshot Completed Detail Page =====================

    public record CompletedDetailResponse(
        Long snapshotId,
        String stockName,                   // 종목 이름
        LocalDate startDate,
        LocalDate endDate,
        String period,                      // 기간 (시작일 - 종료일)
        SummaryResult summaryResult,        // Summary Section
        SnapshotRecord startRecord,         // Start Record Section
        SnapshotRecord endRecord            // End Record Section
    ) {
        public record SummaryResult(
            BigDecimal periodScore,             // 기간 점수 결과
            String modelEvaluation,             // 모델 평가
            String movingAverageInfo,           // 이동평균선 정보
            BigDecimal expectedReturnDiff       // 기대수익률과의 결과 차이
        ) {}
    }
}
