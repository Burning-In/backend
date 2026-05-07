package com.momentum.infrastructure.ma;

import com.momentum.domain.ma.StockMovingAverage;
import com.momentum.domain.ma.StockMovingAverageRepository;
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
}
