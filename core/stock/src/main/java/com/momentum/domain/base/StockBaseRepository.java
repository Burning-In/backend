package com.momentum.domain.base;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase stockBase);

  Optional<StockBase> findCurrentBaseWithLines(Stock stock);

  List<StockBase> findAllByStockOrderByStartedAt(Stock stock);

  Optional<StockBase> findPreviousBase(Stock stock, LocalDate currentBaseStartedAt);

  Optional<StockBase> findWithAnchorPointsById(Long baseId);
}
