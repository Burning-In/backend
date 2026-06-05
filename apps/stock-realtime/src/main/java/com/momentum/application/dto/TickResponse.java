package com.momentum.application.dto;

public record TickResponse(
    String stockCode,
    String tradeTime,
    long currentPrice,
    String priceSign,
    long priceChange,
    double changeRate,
    double weightedAveragePrice,
    long openPrice,
    long highPrice,
    long lowPrice,
    long bestAskPrice,
    long bestBidPrice,
    long tradeVolume,
    long accumulatedVolume,
    double tradeStrength
) {

  public static TickResponse from(StockTickInfo tickInfo) {
    return new TickResponse(
        tickInfo.stockCode(),
        tickInfo.tradeTime(),
        tickInfo.currentPrice(),
        tickInfo.priceSign(),
        tickInfo.priceChange(),
        tickInfo.changeRate(),
        tickInfo.weightedAveragePrice(),
        tickInfo.openPrice(),
        tickInfo.highPrice(),
        tickInfo.lowPrice(),
        tickInfo.bestAskPrice(),
        tickInfo.bestBidPrice(),
        tickInfo.tradeVolume(),
        tickInfo.accumulatedVolume(),
        tickInfo.tradeStrength()
    );
  }
}
