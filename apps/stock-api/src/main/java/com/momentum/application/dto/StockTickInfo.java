package com.momentum.application.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.momentum.infrastructure.lsinvestment.dto.stocktick.LsWsResponse;

public record StockTickInfo(

    @JsonProperty("MKSC_SHRN_ISCD")
    String stockCode,

    @JsonProperty("STCK_CNTG_HOUR")
    String tradeTime,

    @JsonProperty("STCK_PRPR")
    long currentPrice,

    @JsonProperty("PRDY_VRSS_SIGN")
    String priceSign,

    @JsonProperty("PRDY_VRSS")
    long priceChange,

    @JsonProperty("PRDY_CTRT")
    double changeRate,

    @JsonProperty("WGHN_AVRG_STCK_PRC")
    double weightedAveragePrice,

    @JsonProperty("STCK_OPRC")
    long openPrice,

    @JsonProperty("STCK_HGPR")
    long highPrice,

    @JsonProperty("STCK_LWPR")
    long lowPrice,

    @JsonProperty("ASKP1")
    long bestAskPrice,

    @JsonProperty("BIDP1")
    long bestBidPrice,

    @JsonProperty("CNTG_VOL")
    long tradeVolume,

    @JsonProperty("ACML_VOL")
    long accumulatedVolume

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
        body.accumulatedVolume()       // 누적거래량
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
