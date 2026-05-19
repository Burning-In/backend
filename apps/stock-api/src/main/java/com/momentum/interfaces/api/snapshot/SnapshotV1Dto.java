package com.momentum.interfaces.api.snapshot;

import com.momentum.domain.stock.StockRegime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SnapshotV1Dto {

    // ===================== Snapshot List Page =====================

    public enum SnapshotJudgment {
        BUY, SELL, WATCH
    }

    public record SnapshotListResponse(
        int buyCount,
        int sellCount,
        int watchCount,
        List<SnapshotListItem> snapshots
    ) {
        public record SnapshotListItem(
            Long snapshotId,
            String stockName,
            StockRegime stockRegime,
            SnapshotJudgment judgment,
            LocalDateTime recordedAt,
            Long price
        ) {}
    }

    // ===================== Snapshot Detail =====================

    public record SnapshotDetailResponse(
        List<Long> referenceSnapshotIds,
        LocalDateTime recordedAt,
        String retrospective
    ) {}

    // ===================== Snapshot Editor Overlay =====================

    // 스냅샷 생성 요청 (시작)
    public record SnapshotCreateRequest(
        Long stockId,
        SnapshotJudgment judgment,
        List<Long> referenceSnapshotIds,
        String retrospective
    ) {}

    public record SnapshotCreateResponse(
        Long snapshotId,
        LocalDate startDate
    ) {}

    // 스냅샷 수정 요청
    public record SnapshotUpdateRequest(
        Long snapshotId,
        SnapshotJudgment judgment,
        List<Long> referenceSnapshotIds,
        String retrospective
    ) {}
}
