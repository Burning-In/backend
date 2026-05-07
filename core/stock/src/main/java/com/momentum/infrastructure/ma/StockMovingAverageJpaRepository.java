package com.momentum.infrastructure.ma;

import com.momentum.domain.ma.StockMovingAverage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovingAverageJpaRepository extends JpaRepository<StockMovingAverage, Long> {

}
