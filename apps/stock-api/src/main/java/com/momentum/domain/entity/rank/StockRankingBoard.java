package com.momentum.domain.entity.rank;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.StockState;
import jakarta.persistence.Entity;

@Entity
public class StockRankingBoard extends BaseEntity {

  private StockState stockState;
  private Long totalCount;
  private Long totalAlpha;
}
