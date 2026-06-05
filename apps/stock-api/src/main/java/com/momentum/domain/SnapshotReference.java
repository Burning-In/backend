package com.momentum.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 스냅샷 간 참조 링크 (N:M 자기참조 조인 엔티티).
 *
 * <p>BaseEntity를 상속해 링크 단위의 타임스탬프·소프트삭제를 가진다. 순수 {@code @ManyToMany} 대신
 * 조인 엔티티로 두어 링크 자체를 관리/확장할 수 있다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnapshotReference extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "snapshot_id")
  private StockSnapShot snapshot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referenced_snapshot_id")
  private StockSnapShot referenced;

  SnapshotReference(StockSnapShot snapshot, StockSnapShot referenced) {
    this.snapshot = snapshot;
    this.referenced = referenced;
  }
}
