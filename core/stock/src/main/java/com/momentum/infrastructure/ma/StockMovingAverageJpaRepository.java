package com.momentum.infrastructure.ma;

import com.momentum.domain.movingaverage.StockMovingAverage;
import com.momentum.domain.stock.Stock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovingAverageJpaRepository extends JpaRepository<StockMovingAverage, Long> {

  List<StockMovingAverage> findByStockAndDeletedAtIsNull(Stock stock);
}
