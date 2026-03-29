package com.momentum.infrastructure;

import com.momentum.domain.entity.StockDailyCandle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockDailyCandleJpaRepository extends JpaRepository<StockDailyCandle, Long> {

}
