package com.momentum.domain.base;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import java.time.Instant;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase stockBase);

  Optional<StockBase> findCurrentBaseWithLines(Stock stock);

  Optional<StockBase> findPreviousBase(Stock stock, Instant currentBaseCreatedAt);

  Optional<StockBase> findWithPricePointsById(Long baseId);
}
