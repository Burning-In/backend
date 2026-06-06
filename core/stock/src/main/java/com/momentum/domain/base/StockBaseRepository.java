package com.momentum.domain.base;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase stockBase);

  Optional<StockBase> findCurrentBaseWithLines(Stock stock);

  /** 종목의 모든 베이스를 createdAt 오름차순으로 조회한다. */
  List<StockBase> findAllByStockOrderByCreatedAt(Stock stock);

  Optional<StockBase> findPreviousBase(Stock stock, Instant currentBaseCreatedAt);

  Optional<StockBase> findWithPricePointsById(Long baseId);
}
