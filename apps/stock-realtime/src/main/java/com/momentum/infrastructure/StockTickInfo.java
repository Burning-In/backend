package com.momentum.infrastructure;


public record StockTickInfo(
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

  public static StockTickInfo from(LsWsResponse response) {
    LsWsResponse.Body body = response.body();
    return new StockTickInfo(
        body.stockCode(),              // 종목코드
        body.tradeTime(),              // 체결시간
        body.currentPrice(),           // 현재가
        body.priceSign(),              // 전일대비구분
        body.priceChange(),            // 전일대비
        body.changeRate(),             // 등락률
        body.weightedAveragePrice(),   // 가중평균가
        body.openPrice(),              // 시가
        body.highPrice(),              // 고가
        body.lowPrice(),               // 저가
        body.bestAskPrice(),           // 매도호가
        body.bestBidPrice(),           // 매수호가
        body.tradeVolume(),            // 체결량
        body.accumulatedVolume(),       // 누적거래량
        body.tradeStrength()
    );
  }

  private static long parseLong(String v) {

    if (v == null || v.isEmpty()) {
      return 0;
    }

    return Long.parseLong(v);
  }

  private static double parseDouble(String v) {

    if (v == null || v.isEmpty()) {
      return 0.0;
    }

    return Double.parseDouble(v);
  }
}
