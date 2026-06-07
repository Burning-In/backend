package com.momentum.infrastructure.auth;

import com.momentum.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * accessToken / refreshToken(JWT, HS256) 발급 및 검증.
 * 토큰의 subject 에 회원 ID 를 담는다. 서버는 무상태로 서명만 검증한다.
 */
@Component
public class JwtProvider {

  private final SecretKey key;
  private final Duration accessTokenValidity;
  private final Duration refreshTokenValidity;

  public JwtProvider(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    this.accessTokenValidity = jwtProperties.accessTokenValidity();
    this.refreshTokenValidity = jwtProperties.refreshTokenValidity();
  }

  public String createAccessToken(Long memberId) {
    return createToken(memberId, accessTokenValidity);
  }

  public String createRefreshToken(Long memberId) {
    return createToken(memberId, refreshTokenValidity);
  }

  /** 토큰이 유효하면 회원 ID 를, 만료/위변조 시 비어있는 Optional 을 반환한다. */
  public Optional<Long> resolveMemberId(String token) {
    try {
      String subject = Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload()
          .getSubject();
      return Optional.of(Long.valueOf(subject));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private String createToken(Long memberId, Duration validity) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + validity.toMillis());
    return Jwts.builder()
        .subject(String.valueOf(memberId))
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }
}
