package com.momentum.infrastructure.pricepoint;

import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import org.springframework.data.repository.CrudRepository;

public interface StockPricePointCalculationJpaRepository extends CrudRepository<StockPricePointCalculation, Long> {

}
