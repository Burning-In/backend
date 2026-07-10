package com.momentum.domain.movingaverage;

import com.momentum.domain.stock.Stock;
import java.util.List;

public interface StockMovingAverageRepository {

  List<StockMovingAverage> saveAll(List<StockMovingAverage> stockMovingAverages);

  List<StockMovingAverage> findLatestByStock(Stock stock);
}
