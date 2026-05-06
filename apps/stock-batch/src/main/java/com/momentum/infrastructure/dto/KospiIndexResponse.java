package com.momentum.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KospiIndexResponse(

    @JsonProperty("t1485OutBlock")
    OutBlock outBlock

) {

  public record OutBlock(

      @JsonProperty("pricejisu")
      String pricejisu

  ) {}
}
