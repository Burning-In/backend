package com.momentum.infrastructure.anchorpoint;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAnchorPointJpaRepository extends JpaRepository<StockAnchorPoint, Long> {

}
