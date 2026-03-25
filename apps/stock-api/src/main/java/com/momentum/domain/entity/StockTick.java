package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class StockTick extends BaseEntity {

  private String tradeTime;
  private Long price;
  private Long volume;
  private Long accVolume;
  private StockCode stockCode;
}
