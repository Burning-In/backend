package com.momentum.infrastructure.snapshot;

import com.momentum.domain.StockSnapShot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotJpaRepository extends JpaRepository<StockSnapShot, Long> {
}
