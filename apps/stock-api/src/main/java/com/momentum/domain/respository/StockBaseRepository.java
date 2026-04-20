package com.momentum.domain.respository;

import com.momentum.domain.entity.indicator.price.StockBase;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase stockBase);

  List<StockBase> saveAll(List<StockBase> stockBases);

  Optional<StockBase> findCurrentBaseWithLines(Long stockId);

  Optional<StockBase> findPreviousBase(Long stockId, Instant currentBaseCreatedAt);

  Optional<StockBase> findWithPricePointsById(Long baseId);
}
