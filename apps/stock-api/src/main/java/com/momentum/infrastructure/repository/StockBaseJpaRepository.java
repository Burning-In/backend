package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.analysis.base.StockBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockBaseJpaRepository extends JpaRepository<StockBase, Long> {

}
