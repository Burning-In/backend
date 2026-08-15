package com.momentum.domain.eps;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.YearMonth;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockEps extends BaseEntity {

  private double eps;

  private YearMonth quarter;

  private Double yearOverYearChangeRate;

  @ManyToOne
  private Stock stock;

  public StockEps(double eps, YearMonth quarter, Double yearOverYearChangeRate, Stock stock) {
    this.eps = eps;
    this.quarter = quarter;
    this.yearOverYearChangeRate = yearOverYearChangeRate;
    this.stock = stock;
  }
}
