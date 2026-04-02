package com.momentum.infrastructure.lsinvestment.dto.stocktick;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LsWsResponse(
    Header header,
    Body body
) {

  public record Header(

      @JsonProperty("tr_cd")
      String transactionCode,

      @JsonProperty("tr_key")
      String transactionKey
  ) {

  }

  public record Body(

      @JsonProperty("chetime")
      String tradeTime,

      @JsonProperty("sign")
      String priceSign,

      @JsonProperty("change")
      long priceChange,

      @JsonProperty("drate")
      double changeRate,

      @JsonProperty("price")
      long currentPrice,

      @JsonProperty("opentime")
      String openTime,

      @JsonProperty("open")
      long openPrice,

      @JsonProperty("hightime")
      String highTime,

      @JsonProperty("high")
      long highPrice,

      @JsonProperty("lowtime")
      String lowTime,

      @JsonProperty("low")
      long lowPrice,

      @JsonProperty("cgubun")
      String tradeType,

      @JsonProperty("cvolume")
      long tradeVolume,

      @JsonProperty("volume")
      long accumulatedVolume,

      @JsonProperty("value")
      long accumulatedTradingValue,

      @JsonProperty("mdvolume")
      long accumulatedSellVolume,

      @JsonProperty("mdchecnt")
      long accumulatedSellTradeCount,

      @JsonProperty("msvolume")
      long accumulatedBuyVolume,

      @JsonProperty("mschecnt")
      long accumulatedBuyTradeCount,

      @JsonProperty("cpower")
      double tradeStrength,

      @JsonProperty("w_avrg")
      long weightedAveragePrice,

      @JsonProperty("offerho")
      long bestAskPrice,

      @JsonProperty("bidho")
      long bestBidPrice,

      @JsonProperty("status")
      String marketStatus,

      @JsonProperty("jnilvolume")
      long yesterdaySameTimeVolume,

      @JsonProperty("shcode")
      String stockCode,

      @JsonProperty("exchname")
      String exchangeName,

      @JsonProperty("ex_shcode")
      String exchangeStockCode
  ) {

  }
}
