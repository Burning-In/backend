package com.momentum.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

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
