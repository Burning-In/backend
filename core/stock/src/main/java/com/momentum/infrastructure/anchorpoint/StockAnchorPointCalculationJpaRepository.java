package com.momentum.infrastructure.anchorpoint;

import com.momentum.domain.anchorpoint.entity.StockAnchorPointCalculation;
import org.springframework.data.repository.CrudRepository;

public interface StockAnchorPointCalculationJpaRepository extends CrudRepository<StockAnchorPointCalculation, Long> {

}
