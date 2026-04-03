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

  public Stock(String name, String code) {
    this.name = name;
    this.code = code;
    this.stockState = StockState.UNDEFIED;
    this.stockTrend = StockTrend.OTHER;
  }
}
