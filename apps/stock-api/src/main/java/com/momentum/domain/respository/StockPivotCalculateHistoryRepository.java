package com.momentum.domain.respository;

import com.momentum.domain.entity.stock.Stock;
import com.momentum.domain.entity.analysis.pivot.StockPivotCalculateHistory;
import java.util.Optional;

public interface StockPivotCalculateHistoryRepository {

  Optional<StockPivotCalculateHistory> findTopCalculationHistory(Stock stock);

  StockPivotCalculateHistory save(StockPivotCalculateHistory stockPivotCalculateHistory);
}
