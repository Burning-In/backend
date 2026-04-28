package com.momentum.infrastructure.dto;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public record LsTokenRequest(
    String grantType,
    String appkey,
    String appsecretkey,
    String scope
) {

  public static LsTokenRequest create(String appKey, String secret) {
    return new LsTokenRequest(
        "client_credentials",
        appKey,
        secret,
        "oob"
    );
  }

  public MultiValueMap<String, String> toFormData() {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", grantType);
    map.add("appkey", appkey);
    map.add("appsecretkey", appsecretkey);
    map.add("scope", scope);
    return map;
  }
}
