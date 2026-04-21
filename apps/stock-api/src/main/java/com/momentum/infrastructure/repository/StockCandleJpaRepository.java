package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.stock.StockDailyCandle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockCandleJpaRepository extends JpaRepository<StockDailyCandle, Long> {

}
