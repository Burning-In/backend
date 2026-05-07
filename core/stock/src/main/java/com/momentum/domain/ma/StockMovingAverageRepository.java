package com.momentum.domain.ma;

import java.util.List;

public interface StockMovingAverageRepository {

  List<StockMovingAverage> saveAll(List<StockMovingAverage> stockMovingAverages);
}
