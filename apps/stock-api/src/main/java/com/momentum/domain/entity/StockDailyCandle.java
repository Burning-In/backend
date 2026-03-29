package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.entity.indicator.StockPivotType;
import com.momentum.domain.entity.indicator.StockPriceTrend;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

@Entity
public class StockDailyCandle extends BaseEntity {

  private Long openPrice;
  private Long highPrice;
  private Long lowPrice;
  private Long closePrice;

  private Long volume;
  private Long accVolume;

  @Enumerated(EnumType.STRING)
  private StockPriceTrend stockPriceTrend;

  @Enumerated(EnumType.STRING)
  private StockPivotType stockPivotType;

  @ManyToOne
  private Stock stock;
}
