package com.momentum.infrastructure.auth.kinvestment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthTokenRequest(
    @JsonProperty("grant_type")
    String grantType,
    @JsonProperty("appkey")
    String appKey,
    @JsonProperty("appsecret")
    String appSecret
) {

  public static AuthTokenRequest of(String appKey, String secretKey) {
    return new AuthTokenRequest(
        "client_credentials",
        appKey,
        secretKey
    );
  }
}
