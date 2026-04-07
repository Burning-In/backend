package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTick extends BaseEntity {

  private String tradeTime;
  private Long price;
  private Long volume;
  private Long accVolume;
  private Double contractPower;
  private StockCode stockCode;

  public StockTick(String tradeTime, Long price, Long volume, Long accVolume, Double contractPower, StockCode stockCode) {
    this.tradeTime = tradeTime;
    this.price = price;
    this.volume = volume;
    this.accVolume = accVolume;
    this.contractPower = contractPower;
    this.stockCode = stockCode;
  }
}
