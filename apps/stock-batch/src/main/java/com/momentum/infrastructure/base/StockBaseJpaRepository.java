package com.momentum.infrastructure.base;

import com.momentum.domain.base.entity.StockBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockBaseJpaRepository extends JpaRepository<StockBase, Long> {

}
