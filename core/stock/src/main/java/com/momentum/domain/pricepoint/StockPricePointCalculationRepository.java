package com.momentum.domain.pricepoint;

import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.stock.Stock;
import java.util.Optional;

public interface StockPricePointCalculationRepository {

  Optional<StockPricePointCalculation> findLastCalculationHistory(Stock stock);

  StockPricePointCalculation save(StockPricePointCalculation stockPricePointCalculation);
}
