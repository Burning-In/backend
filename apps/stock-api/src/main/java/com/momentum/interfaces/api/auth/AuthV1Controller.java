package com.momentum.interfaces.api.auth;

import com.momentum.application.AuthService;
import com.momentum.application.AuthTokens;
import com.momentum.config.JwtProperties;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.AccountResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.RefreshResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
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

  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
  private static final String REFRESH_TOKEN_PATH = "/api/v1/auth";
  private static final String CSRF_COOKIE = "csrfToken";

  private final AuthService authService;
  private final JwtProperties jwtProperties;
  private final CsrfTokenRepository csrfTokenRepository;

  @GetMapping("/csrf")
  @Override
  public ResponseEntity<ApiResponse<Void>> csrf(HttpServletRequest request) {
    CsrfToken token = csrfTokenRepository.generateToken(request);
    // CSRF 토큰은 프론트가 읽어 X-CSRF-Token 헤더로 echo 해야 하므로 HttpOnly 가 아니다.
    ResponseCookie cookie = ResponseCookie.from(CSRF_COOKIE, token.getToken())
        .httpOnly(false)
        .path("/")
        .sameSite("Lax")
        .build();
    return withCookie(cookie, null);
  }

  @PostMapping("/login")
  @Override
  public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
    AuthTokens tokens = authService.login(request);
    return withCookie(refreshTokenCookie(tokens.refreshToken(), jwtProperties.refreshTokenValidity()),
        new LoginResponse(tokens.accessToken()));
  }

  @PostMapping("/register")
  @Override
  public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody RegisterRequest request) {
    AuthTokens tokens = authService.register(request);
    return withCookie(refreshTokenCookie(tokens.refreshToken(), jwtProperties.refreshTokenValidity()),
        new RegisterResponse(tokens.accessToken()));
  }

  @PostMapping("/email/find")
  @Override
  public ApiResponse<FindEmailResponse> findEmail(@RequestBody FindEmailRequest request) {
    return ApiResponse.success(authService.findEmail(request));
  }

  @PostMapping("/password/find")
  @Override
  public ApiResponse<Void> findPassword(@RequestBody FindPasswordRequest request) {
    authService.findPassword(request);
    return ApiResponse.success(null);
  }

  @PostMapping("/refresh")
  @Override
  public ApiResponse<RefreshResponse> refresh(
      @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
  ) {
    String accessToken = authService.refreshAccessToken(refreshToken);
    return ApiResponse.success(new RefreshResponse(accessToken));
  }

  @PostMapping("/logout")
  @Override
  public ResponseEntity<ApiResponse<Void>> logout() {
    // maxAge 0 인 빈 쿠키로 refreshToken 을 만료시킨다.
    return withCookie(refreshTokenCookie("", Duration.ZERO), null);
  }

  @GetMapping("/account")
  @Override
  public ApiResponse<AccountResponse> getAccount(@AuthenticationPrincipal Long memberId) {
    return ApiResponse.success(authService.getAccount(memberId));
  }

  private ResponseCookie refreshTokenCookie(String value, Duration maxAge) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
        .httpOnly(true)
        .path(REFRESH_TOKEN_PATH)
        .maxAge(maxAge)
        .sameSite("Lax")
        .build();
  }

  private <T> ResponseEntity<ApiResponse<T>> withCookie(ResponseCookie cookie, T data) {
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(data));
  }
}
