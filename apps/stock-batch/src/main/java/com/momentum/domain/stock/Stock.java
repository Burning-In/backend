package com.momentum.domain.stock;

import static com.momentum.sharedkernel.StockRegime.UNKNOWN;

import com.momentum.domain.BaseEntity;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
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
public class Stock extends BaseEntity {

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
    this.stockRegime = stockRegime;
  }

  public void updateTrend(int rsScore, int upTrendThreshold) {
    StockTrend newStockTrend = StockTrend.OTHER;
    if (rsScore >= upTrendThreshold) {
      newStockTrend = StockTrend.UPTREND;
    }
    if (this.stockTrend.equals(newStockTrend)) {
      return;
    }
    this.stockTrend = newStockTrend;
  }
}
