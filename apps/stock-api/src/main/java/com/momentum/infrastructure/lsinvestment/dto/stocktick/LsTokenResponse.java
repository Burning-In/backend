package com.momentum.infrastructure.lsinvestment.dto.stocktick;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LsTokenResponse(

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("expires_in")
    long expiresIn,

    String scope,

    @JsonProperty("token_type")
    String tokenType
) {}
