package com.momentum.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StockChartInfoResponse(

    @JsonProperty("t8451OutBlock1")
    List<CandleResponse> candleResponses

) {

  public record CandleResponse(

      @JsonProperty("date")
      String date,

      @JsonProperty("open")
      long openPrice,

      @JsonProperty("high")
      long highPrice,

      @JsonProperty("low")
      long lowPrice,

      @JsonProperty("close")
      long closePrice,

      @JsonProperty("jdiff_vol")
      long volume,

      @JsonProperty("value")
      long tradingValue,

      @JsonProperty("sign")
      String priceChangeSign

  ) {}
}
