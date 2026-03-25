package com.momentum.domain.rank;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockState extends BaseEntity {

  private StockStateType currentState;
}
