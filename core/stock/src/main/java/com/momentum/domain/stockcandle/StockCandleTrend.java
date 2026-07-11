package com.momentum.domain.stockcandle;


import lombok.Getter;

@Getter
public enum StockCandleTrend { // 이거는 날려야겠다 쓸모가 없음

  UPPER_LIMIT(1, "상한"),
  UP(2, "상승"),
  FLAT(3, "보합"),
  LOWER_LIMIT(4, "하한"),
  DOWN(5, "하락"),
  NONE(9999, "전날없음");

  private final int code;
  private final String description;

  StockCandleTrend(int code, String description) {
    this.code = code;
    this.description = description;
  }

  public static StockCandleTrend getValue(String code) {
    for (StockCandleTrend t : StockCandleTrend.values()) {
      if (t.code == Integer.parseInt(code)) {
        return t;
      }
    }
    return NONE;
  }

  public boolean isUpper() {
    return this == UPPER_LIMIT || this == UP;
  }

  public boolean isLower() {
    return this == LOWER_LIMIT || this == DOWN;
  }
}
