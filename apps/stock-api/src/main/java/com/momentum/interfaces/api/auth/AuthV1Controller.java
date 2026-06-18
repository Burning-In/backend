package com.momentum.interfaces.api.auth;

import static org.springframework.boot.web.server.Cookie.SameSite.STRICT;

import com.momentum.application.AuthService;
import com.momentum.application.AuthTokens;
import com.momentum.config.JwtProperties;
import com.momentum.infrastructure.auth.JwtAuthenticationFilter;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.AccountResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.ResetPasswordConfirmRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.ResetPasswordVerifyRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthV1Controller implements AuthV1ApiSpec {

  private static final String ACCESS_TOKEN_COOKIE = JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE;
  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
  private static final String PASSWORD_RESET_COOKIE = "passwordResetToken";
  private static final String ACCESS_TOKEN_PATH = "/";
  private static final String REFRESH_TOKEN_PATH = "/api/v1/auth";
  private static final String PASSWORD_RESET_PATH = "/api/v1/auth/password";

  private final AuthService authService;
  private final JwtProperties jwtProperties;

  @PostMapping("/login")
  @Override
  public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest request) {
    return issueTokenCookies(authService.login(request));
  }

  @PostMapping("/register")
  @Override
  public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
    return issueTokenCookies(authService.register(request));
  }

  @PostMapping("/email/find")
  @Override
  public ApiResponse<FindEmailResponse> findEmail(@RequestBody FindEmailRequest request) {
    return ApiResponse.success(authService.findEmail(request));
  }

  @PostMapping("/password/verify")
  @Override
  public ResponseEntity<ApiResponse<Void>> verifyForPasswordReset(
      @RequestBody ResetPasswordVerifyRequest request) {
    String resetToken = authService.verifyForPasswordReset(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, secureCookie(PASSWORD_RESET_COOKIE, resetToken,
            PASSWORD_RESET_PATH, jwtProperties.passwordResetTokenValidity()).toString())
        .body(ApiResponse.success(null));
  }

  @PostMapping("/password/reset")
  @Override
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @CookieValue(name = PASSWORD_RESET_COOKIE, required = false) String resetToken,
      @RequestBody ResetPasswordConfirmRequest request) {
    authService.confirmPasswordReset(resetToken, request);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            secureCookie(PASSWORD_RESET_COOKIE, "", PASSWORD_RESET_PATH, Duration.ZERO).toString())
        .body(ApiResponse.success(null));
  }

  @PostMapping("/refresh")
  @Override
  public ResponseEntity<ApiResponse<Void>> refresh(
      @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
  ) {
    return issueTokenCookies(authService.reissueRefreshToken(refreshToken));
  }

  @PostMapping("/logout")
  @Override
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
  ) {
    authService.logout(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            secureCookie(ACCESS_TOKEN_COOKIE, "", ACCESS_TOKEN_PATH, Duration.ZERO).toString())
        .header(HttpHeaders.SET_COOKIE,
            secureCookie(REFRESH_TOKEN_COOKIE, "", REFRESH_TOKEN_PATH, Duration.ZERO).toString())
        .body(ApiResponse.success(null));
  }

  @GetMapping("/account")
  @Override
  public ApiResponse<AccountResponse> getAccount(@AuthenticationPrincipal Long memberId) {
    return ApiResponse.success(authService.getAccount(memberId));
  }

  private ResponseEntity<ApiResponse<Void>> issueTokenCookies(AuthTokens tokens) {
    ResponseCookie access = secureCookie(ACCESS_TOKEN_COOKIE, tokens.accessToken(),
        ACCESS_TOKEN_PATH, jwtProperties.accessTokenValidity());
    ResponseCookie refresh = secureCookie(REFRESH_TOKEN_COOKIE, tokens.refreshToken(),
        REFRESH_TOKEN_PATH, jwtProperties.refreshTokenValidity());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, access.toString())
        .header(HttpHeaders.SET_COOKIE, refresh.toString())
        .body(ApiResponse.success(null));
  }

  private ResponseCookie secureCookie(String name, String value, String path, Duration maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .path(path)
        .maxAge(maxAge)
        .sameSite(STRICT.attributeValue())
        .build();
  }
}
