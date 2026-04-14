package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.indicator.price.StockPivot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPivotJpaRepository extends JpaRepository<StockPivot, Long> {

}
