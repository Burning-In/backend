package com.momentum.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.config.JwtProperties;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long-0123456789";
  private static final Long MEMBER_ID = 42L;

  private final JwtProvider jwtProvider = new JwtProvider(props(SECRET));

  @Test
  @DisplayName("accessToken 은 발급한 회원 ID 로 다시 해석된다")
  void accessTokenRoundTrip() {
    String token = jwtProvider.createAccessToken(MEMBER_ID, Instant.now());

    assertThat(jwtProvider.resolveMemberId(token)).contains(MEMBER_ID);
  }

  @Test
  @DisplayName("refreshToken 도 회원 ID 로 다시 해석된다")
  void refreshTokenRoundTrip() {
    String token = jwtProvider.createRefreshToken(MEMBER_ID, Instant.now());

    assertThat(jwtProvider.resolveMemberId(token)).contains(MEMBER_ID);
  }

  @Test
  @DisplayName("passwordReset 토큰은 전용 해석기로만 회원 ID 를 돌려준다")
  void passwordResetTokenRoundTrip() {
    String token = jwtProvider.createPasswordResetToken(MEMBER_ID, Instant.now());

    assertThat(jwtProvider.resolvePasswordResetMemberId(token)).contains(MEMBER_ID);
  }

  @Test
  @DisplayName("일반 accessToken 은 passwordReset 해석기에서 거부된다(purpose 불일치)")
  void accessTokenRejectedAsPasswordReset() {
    String accessToken = jwtProvider.createAccessToken(MEMBER_ID, Instant.now());

    assertThat(jwtProvider.resolvePasswordResetMemberId(accessToken)).isEmpty();
  }

  @Test
  @DisplayName("같은 시각·같은 회원으로 발급해도 jti 때문에 토큰 문자열이 다르다")
  void tokensAreUniquePerIssue() {
    Instant fixed = Instant.now();

    String first = jwtProvider.createAccessToken(MEMBER_ID, fixed);
    String second = jwtProvider.createAccessToken(MEMBER_ID, fixed);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  @DisplayName("만료된 토큰은 해석되지 않는다(비어있는 Optional)")
  void expiredTokenIsRejected() {
    String expired = jwtProvider.createAccessToken(MEMBER_ID, Instant.now().minus(Duration.ofHours(1)));

    assertThat(jwtProvider.resolveMemberId(expired)).isEmpty();
  }

  @Test
  @DisplayName("형식이 깨진 토큰은 해석되지 않는다")
  void malformedTokenIsRejected() {
    assertThat(jwtProvider.resolveMemberId("not-a-jwt")).isEmpty();
  }

  @Test
  @DisplayName("다른 키로 서명된 토큰은 거부된다(서명 검증)")
  void tokenSignedWithOtherKeyIsRejected() {
    JwtProvider other = new JwtProvider(props("another-secret-key-also-32-bytes-long-9876543210abc"));
    String foreign = other.createAccessToken(MEMBER_ID, Instant.now());

    assertThat(jwtProvider.resolveMemberId(foreign)).isEmpty();
  }

  private static JwtProperties props(String secret) {
    return new JwtProperties(secret, Duration.ofMinutes(30), Duration.ofDays(14), Duration.ofMinutes(10));
  }
}
