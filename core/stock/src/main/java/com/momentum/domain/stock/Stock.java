package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.UNKNOWN;

import com.momentum.domain.AggregateRoot;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends AggregateRoot {

  private String name;
  private String code;
  @Enumerated(EnumType.STRING)
  private StockRegime stockRegime;
  @Enumerated(EnumType.STRING)
  private StockTrend stockTrend;

  private Stock(String name, String code, StockRegime stockRegime, StockTrend stockTrend) {
    this.name = Objects.requireNonNull(name);
    this.code = Objects.requireNonNull(code);
    this.stockRegime = Objects.requireNonNull(stockRegime);
    this.stockTrend = Objects.requireNonNull(stockTrend);
  }

  public static Stock of(String name, String code, StockRegime stockRegime, StockTrend stockTrend) {
    return new Stock(name, code, stockRegime, stockTrend);
  }

  public void update(StockRegime stockRegime) {
    if (stockRegime == null || this.stockRegime.equals(stockRegime) || stockRegime.equals(UNKNOWN)) {
      return;
    }
    StockRegime from = this.stockRegime;
    this.stockRegime = stockRegime;
    registerEvent(new StockStateChangedEvent(code, from, stockRegime));
  }
}
