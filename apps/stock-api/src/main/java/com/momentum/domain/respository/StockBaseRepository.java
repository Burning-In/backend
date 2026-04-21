package com.momentum.domain.respository;

import com.momentum.domain.entity.analysis.base.StockBase;
import java.time.Instant;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase stockBase);

  Optional<StockBase> findCurrentBaseWithLines(Long stockId);

  Optional<StockBase> findPreviousBase(Long stockId, Instant currentBaseCreatedAt);

  Optional<StockBase> findWithPricePointsById(Long baseId);
}
