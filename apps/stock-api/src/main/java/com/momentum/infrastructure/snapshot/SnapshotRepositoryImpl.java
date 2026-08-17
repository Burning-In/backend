package com.momentum.infrastructure.snapshot;

import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SnapshotRepositoryImpl implements SnapshotRepository {

  private final SnapshotJpaRepository snapshotJpaRepository;

  @Override
  public StockSnapShot save(StockSnapShot snapshot) {
    return snapshotJpaRepository.save(snapshot);
  }

  @Override
  public Optional<StockSnapShot> findById(Long id) {
    return snapshotJpaRepository.findById(id);
  }

  @Override
  public List<StockSnapShot> findAllByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return snapshotJpaRepository.findAllById(ids);
  }
}
