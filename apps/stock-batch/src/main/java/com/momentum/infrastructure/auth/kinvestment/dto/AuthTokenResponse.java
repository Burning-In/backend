package com.momentum.infrastructure.auth.kinvestment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthTokenResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("token_type")
    String tokenType,
    @JsonProperty("expires_in")
    Integer expiresIn
) {
}
