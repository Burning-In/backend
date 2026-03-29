package com.momentum.domain.entity.indicator;

import lombok.Getter;

@Getter
public enum StockPriceTrend {

  UPPER_LIMIT(1, "상한"),
  UP(2, "상승"),
  FLAT(3, "보합"),
  LOWER_LIMIT(4, "하한"),
  DOWN(5, "하락"),
  NONE(9999, "전날없음");

  private final int code;
  private final String description;

  StockPriceTrend(int code, String description) {
    this.code = code;
    this.description = description;
  }

  public static StockPriceTrend getValue(String code) {
    for (StockPriceTrend t : StockPriceTrend.values()) {
      if (t.code == Integer.parseInt(code)) {
        return t;
      }
    }
    return NONE;
  }
}
