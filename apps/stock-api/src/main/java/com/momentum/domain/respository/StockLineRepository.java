package com.momentum.domain.respository;

import com.momentum.domain.entity.indicator.price.StockLine;
import java.util.Optional;

public interface StockLineRepository {

  Optional<StockLine> findTopResistanceInRange(Long stockId, long highPivotPointClosePrice, double thresholdPercent);

  Optional<StockLine> findLowestSupportInRange(Long stockId, long highPivotPointClosePrice, double thresholdPercent);

  StockLine save(StockLine stockLine);
}
