package com.momentum.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 서명 키와 토큰 만료 시간 설정. ({@code security.jwt.*})
 */
@ConfigurationProperties("security.jwt")
public record JwtProperties(
    String secret,
    Duration accessTokenValidity,
    Duration refreshTokenValidity
) {

}
