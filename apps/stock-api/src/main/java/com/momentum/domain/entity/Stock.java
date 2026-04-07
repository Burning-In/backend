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
  private StockState stockState;
  private StockTrend stockTrend;

  public Stock(String name, String code, StockState stockState, StockTrend stockTrend) {
    this.name = name;
    this.code = code;
    this.stockState = stockState;
    this.stockTrend = stockTrend;
  }

  public void update(StockState stockState) {
    this.stockState = stockState;
  }
}
