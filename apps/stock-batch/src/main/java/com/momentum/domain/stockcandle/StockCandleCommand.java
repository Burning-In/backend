package com.momentum.domain.stockcandle;

public record StockCandleCommand(
    String date,
    long openPrice,
    long highPrice,
    long lowPrice,
    long closePrice,
    long volume
) {

  public static StockCandleCommand of(
      String date,
      long openPrice,
      long highPrice,
      long lowPrice,
      long closePrice,
      long volume) {
    return new StockCandleCommand(date, openPrice, highPrice, lowPrice, closePrice, volume);
  }
}
