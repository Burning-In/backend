package com.momentum.infrastructure.pricepoint;

import com.momentum.domain.pricepoint.entity.StockPivotCalculation;
import org.springframework.data.repository.CrudRepository;

public interface StockPricePointCalculationJpaRepository extends CrudRepository<StockPivotCalculation, Long> {

}
