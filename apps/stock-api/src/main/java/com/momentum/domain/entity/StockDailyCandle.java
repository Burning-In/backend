package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class StockDailyCandle extends BaseEntity {

  private Long openPrice;
  private Long highPrice;
  private Long lowPrice;
  private Long closePrice;

  private Long volume;
  private Long accVolume;

  @ManyToOne
  private Stock stock;
}
