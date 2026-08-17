package com.momentum.domain;

import com.momentum.sharedkernel.StockRegime;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockSnapShot extends BaseEntity {

  private Long stockId;

  /**
   * 기록 시점의 레짐 (박제)
   */
  @Enumerated(EnumType.STRING)
  private StockRegime capturedRegime;

  /**
   * 기록 시점의 종가 (박제)
   */
  private long capturedPrice;

  @Enumerated(EnumType.STRING)
  private SnapshotJudgment judgment;

  /**
   * 스냅샷끼리 서로 참조 (N:M 자기참조) — SnapshotReference 조인 엔티티로 관리
   */
  @OneToMany(mappedBy = "snapshot", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<SnapshotReference> references = new ArrayList<>();

  private LocalDateTime recordedAt;

  @Column(length = 2000)
  private String retrospective;

  private StockSnapShot(Long stockId, StockRegime capturedRegime, long capturedPrice,
      SnapshotJudgment judgment, LocalDateTime recordedAt, String retrospective) {
    this.stockId = stockId;
    this.capturedRegime = capturedRegime;
    this.capturedPrice = capturedPrice;
    this.judgment = judgment;
    this.recordedAt = recordedAt;
    this.retrospective = retrospective;
  }

  public static StockSnapShot create(Long stockId, StockRegime capturedRegime, long capturedPrice,
      SnapshotJudgment judgment, List<StockSnapShot> referencedSnapshots, LocalDateTime recordedAt,
      String retrospective) {
    StockSnapShot snapshot = new StockSnapShot(stockId, capturedRegime, capturedPrice,
        judgment, recordedAt, retrospective);
    referencedSnapshots.forEach(snapshot::addReference);
    return snapshot;
  }

  public void update(SnapshotJudgment judgment, List<StockSnapShot> referencedSnapshots,
      String retrospective) {
    this.judgment = judgment;
    this.retrospective = retrospective;
    this.references.clear();
    referencedSnapshots.forEach(this::addReference);
  }

  private void addReference(StockSnapShot referenced) {
    if (isSelf(referenced)) {
      throw new IllegalArgumentException("스냅샷은 자기 자신을 참조할 수 없습니다.");
    }
    this.references.add(new SnapshotReference(this, referenced));
  }

  private boolean isSelf(StockSnapShot other) {
    return this == other || (this.getId() != null && this.getId().equals(other.getId()));
  }
}
