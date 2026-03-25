package com.momentum.domain.indicator;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockResistanceLine  extends BaseEntity {
  private long value;
}
