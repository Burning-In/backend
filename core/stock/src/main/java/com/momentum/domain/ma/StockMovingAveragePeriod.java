package com.momentum.domain.ma;


import lombok.Getter;

@Getter
public enum StockMovingAveragePeriod {

  MA_50(50),
  MA_150(150),
  MA_200(200);

  private final int period;

  StockMovingAveragePeriod(int period) {
    this.period = period;
  }
}
