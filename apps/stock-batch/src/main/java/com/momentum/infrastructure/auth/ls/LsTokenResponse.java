package com.momentum.infrastructure.auth.ls;

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
