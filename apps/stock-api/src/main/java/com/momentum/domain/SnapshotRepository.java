package com.momentum.domain;

import java.util.List;
import java.util.Optional;

public interface SnapshotRepository {

  StockSnapShot save(StockSnapShot snapshot);

  Optional<StockSnapShot> findById(Long id);

  List<StockSnapShot> findAllByIds(List<Long> ids);
}
