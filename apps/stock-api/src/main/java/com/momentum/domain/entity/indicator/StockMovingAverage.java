package com.momentum.domain.entity.indicator;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockMovingAverage extends BaseEntity {

  private Long value;
  private StockMovingAveragePeriod stockMovingAveragePeriod;
}
