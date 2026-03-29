package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.StockCandle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockCandleJpaRepository extends JpaRepository<StockCandle, Long> {

}
