package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

  private String name;
  private String code;
  private StockRegime stockRegime;
  private StockTrend stockTrend;

  public Stock(String name, String code, StockRegime stockRegime, StockTrend stockTrend) {
    this.name = name;
    this.code = code;
    this.stockRegime = stockRegime;
    this.stockTrend = stockTrend;
  }

  public void update(StockRegime stockRegime) {
    this.stockRegime = stockRegime;
  }
}
