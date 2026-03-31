package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.indicator.StockLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLineJpaRepository extends JpaRepository<StockLine, Long> {

}
