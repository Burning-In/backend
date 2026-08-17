package com.momentum.infrastructure.query;

import java.time.LocalDateTime;

public final class SnapshotRows {

  private SnapshotRows() {
  }

  public record SnapshotSourceRow(Long stockId, String stockRegime, long closePrice) {

  }

  public record SnapshotListRow(
      Long snapshotId,
      String stockName,
      String stockCode,
      String capturedRegime,
      String judgment,
      LocalDateTime recordedAt,
      long capturedPrice) {

  }
}
