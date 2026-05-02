package com.momentum.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LsWsRequest(
    Header header,
    Body body
) {

  private static final String TR_TYPE_SUBSCRIBE = "3";
  private static final String TR_TYPE_UNSUBSCRIBE = "4";

  private static final String TR_CD = "US3";

  private static final String CODE_PREFIX = "U";
  private static final String CODE_PADDING = "   ";

  public static LsWsRequest subscribe(
      String token,
      String stockCode
  ) {

    return new LsWsRequest(
        new Header(token, TR_TYPE_SUBSCRIBE),
        new Body(TR_CD, CODE_PREFIX + stockCode + CODE_PADDING)
    );
  }

  public static LsWsRequest unsubscribe(
      String token,
      String stockCode
  ) {
    return new LsWsRequest(
        new Header(token, TR_TYPE_UNSUBSCRIBE),
        new Body(TR_CD, CODE_PREFIX + stockCode + CODE_PADDING)
    );
  }

  private record Header(
      @JsonProperty("token")
      String token,

      @JsonProperty("tr_type")
      String tradeType
  ) {}

  private record Body(
      @JsonProperty("tr_cd")
      String tradeCode,

      @JsonProperty("tr_key")
      String tradeKey
  ) {}
}
