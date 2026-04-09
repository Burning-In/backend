package com.momentum.domain.entity.rank;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.StockRegime;
import jakarta.persistence.Entity;

@Entity
public class StockRankingBoard extends BaseEntity {

  private StockRegime stockRegime;
  private Long totalCount;
  private Long totalAlpha;
}
