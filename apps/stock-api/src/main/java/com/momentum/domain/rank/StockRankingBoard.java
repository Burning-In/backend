package com.momentum.domain.rank;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockRankingBoard extends BaseEntity {

  private StockStateType stockStateType;
  private Long totalCount;
  private Long totalAlpha;
}
