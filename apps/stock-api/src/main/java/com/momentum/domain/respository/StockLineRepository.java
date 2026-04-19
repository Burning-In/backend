package com.momentum.domain.respository;

import com.momentum.domain.entity.indicator.price.StockBaseLine;
import java.util.Optional;

public interface StockLineRepository {

  Optional<StockBaseLine> findTopResistanceInRange(Long stockId, long highPivotPointClosePrice, double thresholdPercent);

  Optional<StockBaseLine> findLowestSupportInRange(Long stockId, long highPivotPointClosePrice, double thresholdPercent);

  StockBaseLine save(StockBaseLine stockBaseLine);

  Optional<StockBaseLine> findLastResistance(Long stockId);
}
