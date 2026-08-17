package com.momentum.infrastructure.stockcandle;

import com.momentum.domain.stockcandle.StockDailyCandle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockCandleJpaRepository extends JpaRepository<StockDailyCandle, Long> {

}
