package com.momentum.infrastructure.pricepoint;

import com.momentum.domain.pricepoint.entity.StockPricePoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPricePointJpaRepository extends JpaRepository<StockPricePoint, Long> {

}
