package com.momentum.domain.pricepoint;

import com.momentum.domain.pricepoint.entity.StockPivotCalculation;
import com.momentum.domain.stock.Stock;
import java.util.Optional;

public interface StockPricePointCalculationRepository {

  Optional<StockPivotCalculation> findTopCalculationHistory(Stock stock);

  StockPivotCalculation save(StockPivotCalculation stockPivotCalculation);
}
