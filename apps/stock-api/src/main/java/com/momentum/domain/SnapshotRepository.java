package com.momentum.domain;

import com.momentum.domain.stock.StockRegime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SnapshotRepository {

  StockSnapShot save(StockSnapShot snapshot);

  Optional<StockSnapShot> findById(Long id);

  List<StockSnapShot> findAllByIds(List<Long> ids);

  List<StockSnapShot> search(
      LocalDateTime startDate,
      LocalDateTime endDate,
      List<SnapshotJudgment> judgments,
      List<StockRegime> regimes,
      String stockName
  );
}
