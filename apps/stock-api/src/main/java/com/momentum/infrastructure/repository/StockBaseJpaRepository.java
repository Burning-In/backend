package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.indicator.price.StockBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockBaseJpaRepository extends JpaRepository<StockBase, Long> {

}
