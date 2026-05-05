package com.momentum.interfaces.api.realtime;

public class StockRealtimeV1Dto {

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

  }
}
