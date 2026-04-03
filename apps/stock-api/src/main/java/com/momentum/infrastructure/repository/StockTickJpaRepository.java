package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.StockTick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTickJpaRepository extends JpaRepository<StockTick, Long> {

}
