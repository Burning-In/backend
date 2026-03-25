package com.momentum.domain.entity.indicator;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockBaseStage extends BaseEntity {

  private double highPrice;
  private double lovPrice;
  private int currentCount;
}
