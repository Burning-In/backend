package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.indicator.price.StockPricePoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPricePointJpaRepository extends JpaRepository<StockPricePoint, Long> {

}
