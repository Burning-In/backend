package com.momentum.infrastructure.auth;

import com.momentum.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  private static final String PURPOSE_CLAIM = "purpose";
  private static final String PASSWORD_RESET_PURPOSE = "PASSWORD_RESET";

  private final SecretKey key;
  private final Duration accessTokenValidity;
  private final Duration refreshTokenValidity;
  private final Duration passwordResetTokenValidity;

  public JwtProvider(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    this.accessTokenValidity = jwtProperties.accessTokenValidity();
    this.refreshTokenValidity = jwtProperties.refreshTokenValidity();
    this.passwordResetTokenValidity = jwtProperties.passwordResetTokenValidity();
  }

  public String createAccessToken(Long memberId, Instant now) {
    return createToken(memberId, accessTokenValidity, now);
  }

  public String createRefreshToken(Long memberId, Instant now) {
    return createToken(memberId, refreshTokenValidity, now);
  }

  public String createPasswordResetToken(Long memberId, Instant now) {
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(memberId))
        .claim(PURPOSE_CLAIM, PASSWORD_RESET_PURPOSE)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(passwordResetTokenValidity)))
        .signWith(key)
        .compact();
  }

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

  public Optional<Long> resolvePasswordResetMemberId(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      if (!PASSWORD_RESET_PURPOSE.equals(claims.get(PURPOSE_CLAIM, String.class))) {
        return Optional.empty();
      }
      return Optional.of(Long.valueOf(claims.getSubject()));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private String createToken(Long memberId, Duration validity, Instant now) {
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(String.valueOf(memberId))
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(validity)))
        .signWith(key)
        .compact();
  }
}
