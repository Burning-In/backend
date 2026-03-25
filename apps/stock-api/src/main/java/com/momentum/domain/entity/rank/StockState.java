package com.momentum.domain.entity.rank;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockState extends BaseEntity {

  private StockStateType currentState;
}
