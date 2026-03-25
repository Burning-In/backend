package com.momentum.domain;

import jakarta.persistence.Entity;

@Entity
public class StockTick extends BaseEntity {

  private String tradeTime;
  private Long price;
  private Long volume;
  private Long accVolume;
  private StockCode stockCode;
}
