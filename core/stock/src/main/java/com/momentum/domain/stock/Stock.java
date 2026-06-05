package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.UNKNOWN;

import com.momentum.domain.AggregateRoot;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends AggregateRoot {

  private String name;
  private String code; // 수정필요
  private StockRegime stockRegime;
  private StockTrend stockTrend;

  public Stock(String name, String code, StockRegime stockRegime, StockTrend stockTrend) {
    this.name = name;
    this.code = code;
    this.stockRegime = stockRegime;
    this.stockTrend = stockTrend;
  }

  public void update(StockRegime stockRegime) {
    if (this.stockRegime.equals(stockRegime) || stockRegime.equals(UNKNOWN)) {
      return;
    }
    StockRegime from = this.stockRegime;
    this.stockRegime = stockRegime;
    registerEvent(new StockStateChangedEvent(code, from, stockRegime));
  }
}
