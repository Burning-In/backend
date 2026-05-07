package com.momentum.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisKospiIndexResponse(

    @JsonProperty("rt_cd")
    String rtCd,

    @JsonProperty("output1")
    Output1 output1

) {

  public record Output1(

      @JsonProperty("bstp_nmix_prpr")
      String bstpNmixPrpr

  ) {}
}
