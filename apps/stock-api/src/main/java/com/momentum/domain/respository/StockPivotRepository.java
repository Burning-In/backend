package com.momentum.domain.respository;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockPivot;
import java.util.List;
import java.util.Optional;

public interface StockPivotRepository {

  StockPivot save(StockPivot stockPivot);

  Optional<StockPivot> findTopByStockOrderByCreatedAtDesc(Stock stock);

  List<StockPivot> findTop3ByStockOrderByCreatedAtDesc(Stock stock);
}
