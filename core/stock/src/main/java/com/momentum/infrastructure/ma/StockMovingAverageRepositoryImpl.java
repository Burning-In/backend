package com.momentum.infrastructure.ma;

import com.momentum.domain.movingaverage.StockMovingAverage;
import com.momentum.domain.movingaverage.StockMovingAverageRepository;
import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockMovingAverageRepositoryImpl implements StockMovingAverageRepository {

  private final StockMovingAverageJpaRepository stockMovingAverageJpaRepository;

  @Override
  public List<StockMovingAverage> saveAll(List<StockMovingAverage> stockMovingAverages) {
    return stockMovingAverageJpaRepository.saveAll(stockMovingAverages);
  }

  @Override
  public List<StockMovingAverage> findLatestByStock(Stock stock, LocalDate baseDate) {
    return stockMovingAverageJpaRepository.findLatestByStockAndBaseDate(stock, baseDate);
  }
}
