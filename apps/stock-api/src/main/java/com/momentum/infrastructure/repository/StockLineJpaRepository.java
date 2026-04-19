package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.indicator.price.StockBaseLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLineJpaRepository extends JpaRepository<StockBaseLine, Long> {

}
