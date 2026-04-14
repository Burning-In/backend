package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.indicator.price.StockPivotCalculateHistory;
import org.springframework.data.repository.CrudRepository;

public interface StockPivotCalculateHistoryJpaRepository extends CrudRepository<StockPivotCalculateHistory, Long> {

}
