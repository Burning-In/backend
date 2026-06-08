package com.momentum.interfaces.api.snapshot;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.stock.StockRegime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SnapshotV1Dto {

  // ===================== Snapshot List Page =====================

  public record SnapshotListResponse(
      int buyCount,
      int sellCount,
      int watchCount,
      List<SnapshotListItem> snapshots
  ) {

    public record SnapshotListItem(
        Long snapshotId,
        String stockName,
        String stockCode,
        StockRegime stockRegime,
        SnapshotJudgment judgment,
        LocalDateTime recordedAt,
        Long price
    ) {

    }
  }

  // ===================== Snapshot Detail =====================

  public record SnapshotDetailResponse(
      String stockName,
      String stockCode,
      SnapshotJudgment judgment,
      List<Long> referenceSnapshotIds,
      LocalDateTime recordedAt,
      String retrospective
  ) {

  }

  // ===================== Snapshot Editor Overlay =====================

  // 스냅샷 생성 요청 (시작)
  public record SnapshotCreateRequest(
      String stockCode,
      SnapshotJudgment judgment,
      List<Long> referenceSnapshotIds,
      String retrospective
  ) {

  }

  public record SnapshotCreateResponse(
      Long snapshotId,
      LocalDate startDate
  ) {

  }

  // 스냅샷 수정 요청
  public record SnapshotUpdateRequest(
      Long snapshotId,
      SnapshotJudgment judgment,
      List<Long> referenceSnapshotIds,
      String retrospective
  ) {

  }
}
