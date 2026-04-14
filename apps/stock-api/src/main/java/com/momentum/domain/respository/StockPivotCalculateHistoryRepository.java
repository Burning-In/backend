package com.momentum.domain.respository;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockPivotCalculateHistory;
import java.util.Optional;

public interface StockPivotCalculateHistoryRepository {

  Optional<StockPivotCalculateHistory> findTopCalculationHistory(Stock stock);

  StockPivotCalculateHistory save(StockPivotCalculateHistory stockPivotCalculateHistory);
}
