package com.momentum.domain.entity.rank;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class StockRank extends BaseEntity {

  private long rankNumber;
  private double alphaRatio;
  @OneToOne
  private Stock stock;
  @OneToOne
  private StockRankingBoard stockRankingBoard;
}
