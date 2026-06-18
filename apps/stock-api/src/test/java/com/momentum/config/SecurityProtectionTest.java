package com.momentum.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.infrastructure.auth.JwtAuthenticationFilter;
import com.momentum.infrastructure.auth.JwtProvider;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SecurityProtectionTest {

  @Autowired
  private MockMvcTester mockMvc;
  @Autowired
  private JwtProvider jwtProvider;

  // ===================== 보호 경로 — 토큰 없으면 401 =====================

  @Test
  @DisplayName("스냅샷 목록은 토큰 없으면 401 + ApiResponse FAIL 포맷")
  void snapshotListRequiresAuth() {
    assertThat(mockMvc.get().uri("/api/v1/snapshots"))
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .bodyJson().extractingPath("$.meta.result").isEqualTo("FAIL");
  }

  @Test
  @DisplayName("스냅샷 상세는 토큰 없으면 401")
  void snapshotDetailRequiresAuth() {
    assertThat(mockMvc.get().uri("/api/v1/snapshots/1"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("스냅샷 생성(POST)은 토큰 없으면 401 (본문 파싱 전에 필터가 차단)")
  void snapshotCreateRequiresAuth() {
    assertThat(mockMvc.post().uri("/api/v1/snapshots"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("관심종목 목록은 토큰 없으면 401")
  void likeListRequiresAuth() {
    assertThat(mockMvc.get().uri("/api/v1/stocks/likes"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("관심종목 추가(POST)는 토큰 없으면 401")
  void likeAddRequiresAuth() {
    assertThat(mockMvc.post().uri("/api/v1/stocks/005930/like"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  // ===================== 보호 경로 — 토큰 유효/무효 =====================

  @Test
  @DisplayName("유효한 accessToken 쿠키면 필터를 통과한다(401 아님)")
  void validTokenPassesFilter() {
    Cookie cookie = accessCookie(jwtProvider.createAccessToken(1L, Instant.now()));

    assertThat(mockMvc.get().uri("/api/v1/snapshots").cookie(cookie))
        .hasStatusOk();
  }

  @Test
  @DisplayName("위조된 토큰이면 401")
  void forgedTokenIsUnauthorized() {
    Cookie cookie = accessCookie("not-a-valid-jwt");

    assertThat(mockMvc.get().uri("/api/v1/snapshots").cookie(cookie))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("만료된 토큰이면 401")
  void expiredTokenIsUnauthorized() {
    // 1시간 전 기준으로 발급 → exp 가 이미 지난 토큰
    Cookie cookie = accessCookie(
        jwtProvider.createAccessToken(1L, Instant.now().minus(Duration.ofHours(1))));

    assertThat(mockMvc.get().uri("/api/v1/snapshots").cookie(cookie))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  // ===================== 공개 경로 — 토큰 없어도 통과 =====================

  @Test
  @DisplayName("계정 조회는 토큰 없으면 401 (보호 경로)")
  void accountRequiresAuth() {
    assertThat(mockMvc.get().uri("/api/v1/auth/account"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("공개 경로(회원가입)는 토큰 없어도 401이 아니다")
  void publicEndpointNotBlocked() {
    assertThat(mockMvc.post().uri("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"pub@momentum.com","password":"pw1234","name":"홍길동","phoneNumber":"01012345678"}
            """))
        .hasStatusOk();
  }

  private Cookie accessCookie(String token) {
    return new Cookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, token);
  }
}
