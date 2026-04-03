package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTick extends BaseEntity {

  private String tradeTime;
  private Long price;
  private Long volume;
  private Long accVolume;
  private StockCode stockCode;

  public StockTick(String tradeTime, Long price, Long volume, Long accVolume, StockCode stockCode) {
    this.tradeTime = tradeTime;
    this.price = price;
    this.volume = volume;
    this.accVolume = accVolume;
    this.stockCode = stockCode;
  }
}
