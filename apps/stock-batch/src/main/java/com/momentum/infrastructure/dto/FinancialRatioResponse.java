package com.momentum.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FinancialRatioResponse(

    @JsonProperty("rt_cd")
    String rtCd,

    @JsonProperty("msg_cd")
    String msgCd,

    @JsonProperty("msg1")
    String msg1,

    @JsonProperty("output")
    List<Output> output

) {

  public record Output(

      @JsonProperty("stac_yymm")
      String stacYymm,

      @JsonProperty("eps")
      String eps

  ) {}
}
