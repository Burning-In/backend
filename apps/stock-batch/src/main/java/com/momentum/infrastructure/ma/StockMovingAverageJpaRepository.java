package com.momentum.infrastructure.ma;

import com.momentum.domain.movingaverage.StockMovingAverage;
import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovingAverageJpaRepository extends JpaRepository<StockMovingAverage, Long> {

  @Query("""
      select movingAverage from StockMovingAverage movingAverage
      where movingAverage.stock = :stock
        and movingAverage.deletedAt is null
        and movingAverage.baseDate = (
          select max(latest.baseDate) from StockMovingAverage latest
          where latest.stock = :stock
            and latest.deletedAt is null
            and latest.baseDate <= :baseDate
        )
      """)
  List<StockMovingAverage> findLatestByStockAndBaseDate(@Param("stock") Stock stock,
      @Param("baseDate") LocalDate baseDate);
}
