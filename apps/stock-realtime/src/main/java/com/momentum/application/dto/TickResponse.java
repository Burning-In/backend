package com.momentum.application.dto;

public record TickResponse(
    String stockCode,
    long currentPrice
) {

  public static TickResponse from(StockTickInfo tickInfo) {
    return new TickResponse(
        tickInfo.stockCode(),
        tickInfo.currentPrice()
    );
  }
}
