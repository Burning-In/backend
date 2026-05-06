package com.momentum.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KospiIndexRequest(

    @JsonProperty("t1485InBlock")
    InBlock input

) {

  public record InBlock(

      @JsonProperty("upcode")
      String upcode,

      @JsonProperty("gubun")
      String gubun

  ) {}

  private static final String KOSPI_CODE = "001";
  private static final String AFTER_MARKET = "2";

  public static KospiIndexRequest afterMarket() {
    return new KospiIndexRequest(new InBlock(KOSPI_CODE, AFTER_MARKET));
  }
}
