package com.momentum.interfaces.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AuthV1ControllerTest {

  @Autowired
  private MockMvcTester mockMvc;

  @Test
  @DisplayName("회원가입은 accessToken/refreshToken HttpOnly 쿠키를 반환한다")
  void registerReturnsAuthCookies() {
    assertThat(mockMvc.post().uri("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registerBody("reg@momentum.com")))
        .hasStatusOk()
        .cookies()
        .containsCookie("accessToken").isHttpOnly("accessToken", true)
        .containsCookie("refreshToken").isHttpOnly("refreshToken", true);
  }

  @Test
  @DisplayName("발급 쿠키는 HttpOnly·Secure·SameSite=Strict 속성을 갖는다")
  void authCookiesHaveSecurityAttributes() {
    MvcTestResult result = register("cookieattr@momentum.com");

    assertThat(result).cookies()
        .isHttpOnly("accessToken", true).isSecure("accessToken", true)
        .isHttpOnly("refreshToken", true).isSecure("refreshToken", true);

    // SameSite 는 CookieMapAssert 에 단언 메서드가 없어 Set-Cookie 헤더로 확인한다.
    List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
    assertThat(setCookies).hasSize(2).allMatch(header -> header.contains("SameSite=Strict"));
  }

  @Test
  @DisplayName("access·refresh 쿠키는 Path 스코프가 다르다")
  void authCookiesHaveScopedPaths() {
    assertThat(register("cookiepath@momentum.com")).cookies()
        .hasPath("accessToken", "/")
        .hasPath("refreshToken", "/api/v1/auth");
  }

  @Test
  @DisplayName("비밀번호 재설정 토큰 쿠키는 /password 경로로 좁혀진다")
  void passwordResetCookieIsScoped() {
    register("cookiereset@momentum.com");

    MvcTestResult result = mockMvc.post().uri("/api/v1/auth/password/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"cookiereset@momentum.com","name":"홍길동","phoneNumber":"01012345678"}
            """)
        .exchange();

    assertThat(result).cookies()
        .isHttpOnly("passwordResetToken", true)
        .isSecure("passwordResetToken", true)
        .hasPath("passwordResetToken", "/api/v1/auth/password");
  }

  @Test
  @DisplayName("로그인은 CSRF 토큰 없이 accessToken/refreshToken 쿠키를 반환한다")
  void loginReturnsAuthCookies() {
    register("loginflow@momentum.com");

    assertThat(mockMvc.post().uri("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginBody("loginflow@momentum.com")))
        .hasStatusOk()
        .cookies()
        .containsCookie("accessToken").containsCookie("refreshToken");
  }

  @Test
  @DisplayName("refreshToken 쿠키로 재발급하면 새 access/refresh 쿠키를 반환한다")
  void refreshReturnsNewAuthCookies() {
    Cookie refreshCookie = register("refreshflow@momentum.com").getResponse()
        .getCookie("refreshToken");

    assertThat(mockMvc.post().uri("/api/v1/auth/refresh").cookie(refreshCookie))
        .hasStatusOk()
        .cookies()
        .containsCookie("accessToken").containsCookie("refreshToken");
  }

  @Test
  @DisplayName("로그아웃은 access/refresh 쿠키를 만료(Max-Age=0)시킨다")
  void logoutClearsAuthCookies() {
    Cookie refreshCookie = register("logoutflow@momentum.com").getResponse()
        .getCookie("refreshToken");

    assertThat(mockMvc.post().uri("/api/v1/auth/logout").cookie(refreshCookie))
        .hasStatusOk()
        .cookies()
        .hasMaxAge("accessToken", Duration.ZERO).hasMaxAge("refreshToken", Duration.ZERO);
  }

  @Test
  @DisplayName("비밀번호 재설정 1단계는 본인확인 성공 시 passwordResetToken 쿠키를 발급한다")
  void passwordResetVerifyIssuesResetCookie() {
    register("pwreset@momentum.com");

    assertThat(mockMvc.post().uri("/api/v1/auth/password/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"pwreset@momentum.com","name":"홍길동","phoneNumber":"01012345678"}
            """))
        .hasStatusOk()
        .cookies()
        .containsCookie("passwordResetToken").isHttpOnly("passwordResetToken", true);
  }

  @Test
  @DisplayName("1단계 쿠키로 2단계 비밀번호 변경에 성공하고 재설정 쿠키가 삭제된다")
  void passwordResetTwoStepFlowSucceeds() {
    register("pwreset2@momentum.com");

    Cookie resetCookie = mockMvc.post().uri("/api/v1/auth/password/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"pwreset2@momentum.com","name":"홍길동","phoneNumber":"01012345678"}
            """)
        .exchange().getResponse().getCookie("passwordResetToken");

    assertThat(mockMvc.post().uri("/api/v1/auth/password/reset").cookie(resetCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"newPassword":"brandnew1234"}
            """))
        .hasStatusOk()
        .cookies().hasMaxAge("passwordResetToken", Duration.ZERO);

    // 변경된 비밀번호로 로그인되는지 확인
    assertThat(mockMvc.post().uri("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email":"pwreset2@momentum.com","password":"brandnew1234"}
            """))
        .hasStatusOk()
        .cookies().containsCookie("accessToken");
  }

  @Test
  @DisplayName("재설정 토큰 쿠키 없이 2단계를 호출하면 401")
  void passwordResetConfirmWithoutTokenIsUnauthorized() {
    assertThat(mockMvc.post().uri("/api/v1/auth/password/reset")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"newPassword":"brandnew1234"}
            """))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("토큰 없이 계정 조회 시 401 (보호 경로)")
  void accountRequiresAuth() {
    assertThat(mockMvc.get().uri("/api/v1/auth/account"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("accessToken 쿠키로 계정 조회 시 로그인 정보를 반환한다")
  void accountWithAccessCookieReturnsInfo() {
    Cookie accessCookie = register("me@momentum.com").getResponse().getCookie("accessToken");

    MvcTestResult result = mockMvc.get().uri("/api/v1/auth/account").cookie(accessCookie).exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.data.isLoggedIn").isEqualTo(true);
    assertThat(result).bodyJson().extractingPath("$.data.userId").isNotNull();
  }

  private MvcTestResult register(String email) {
    return mockMvc.post().uri("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(registerBody(email))
        .exchange();
  }

  private String registerBody(String email) {
    return """
        {"email":"%s","password":"pw1234","name":"홍길동","phoneNumber":"01012345678"}
        """.formatted(email);
  }

  private String loginBody(String email) {
    return """
        {"email":"%s","password":"pw1234"}
        """.formatted(email);
  }
}
