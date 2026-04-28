package com.momentum.domain.base;

import com.momentum.domain.base.entity.StockBase;
import java.time.Instant;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase stockBase);

  Optional<StockBase> findCurrentBaseWithLines(Long stockId);

  Optional<StockBase> findPreviousBase(Long stockId, Instant currentBaseCreatedAt);

  Optional<StockBase> findWithPricePointsById(Long baseId);
}
