package com.momentum.application;

/** 인증 성공 시 발급되는 토큰 쌍. accessToken 은 응답 본문, refreshToken 은 HttpOnly 쿠키로 내려간다. */
public record AuthTokens(
    String accessToken,
    String refreshToken
) {

}
