package com.momentum.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockChartInfoRequest(

    @JsonProperty("t8451InBlock")
    InBlock input

) {

  public record InBlock(

      @JsonProperty("shcode")
      String stockCode,

      @JsonProperty("gubun")
      String periodType,   // 2:일 3:주 4:월 5:년

      @JsonProperty("qrycnt")
      int requestCount,

      @JsonProperty("sdate")
      String startDate,

      @JsonProperty("edate")
      String endDate,

      @JsonProperty("cts_date")
      String continuationDate,

      @JsonProperty("comp_yn")
      String compressYn,

      @JsonProperty("sujung")
      String adjustedPriceYn,

      @JsonProperty("exchgubun")
      String exchangeType

  ) {

  }

  private static final String PERIOD_TYPE_DAILY = "2";

  public static StockChartInfoRequest daily(
      String stockCode,
      int count,
      String startDate,
      String endDate
  ) {
    return new StockChartInfoRequest(
        new StockChartInfoRequest.InBlock(
            stockCode,
            PERIOD_TYPE_DAILY,
            count,
            startDate,
            endDate,
            "",
            "N",
            "Y",
            "U"
        )
    );
  }
}
