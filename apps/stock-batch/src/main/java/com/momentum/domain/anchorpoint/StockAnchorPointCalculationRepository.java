package com.momentum.domain.anchorpoint;

import com.momentum.domain.anchorpoint.entity.StockAnchorPointCalculation;
import com.momentum.domain.stock.Stock;
import java.util.Optional;

public interface StockAnchorPointCalculationRepository {

  Optional<StockAnchorPointCalculation> findLastCalculationHistory(Stock stock);

  StockAnchorPointCalculation save(StockAnchorPointCalculation stockAnchorPointCalculation);
}
