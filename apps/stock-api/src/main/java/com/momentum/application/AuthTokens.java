package com.momentum.application;

public record AuthTokens(
    String accessToken,
    String refreshToken
) {

}
