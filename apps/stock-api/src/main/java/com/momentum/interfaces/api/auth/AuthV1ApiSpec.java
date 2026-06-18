package com.momentum.interfaces.api.auth;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.AccountResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.ResetPasswordConfirmRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.ResetPasswordVerifyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth V1 API", description = "인증/인가 관련 API 입니다.")
public interface AuthV1ApiSpec {

  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인합니다. accessToken/refreshToken 모두 HttpOnly Cookie 로 내려갑니다."
  )
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      headers = {
          @Header(name = "Set-Cookie", description = "accessToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/"),
          @Header(name = "Set-Cookie", description = "refreshToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth")
      }
  )
  ResponseEntity<ApiResponse<Void>> login(LoginRequest request);

  @Operation(
      summary = "회원가입",
      description = "이메일, 비밀번호, 전화번호, 닉네임으로 회원가입합니다. accessToken/refreshToken 모두 HttpOnly Cookie 로 내려갑니다."
  )
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      headers = {
          @Header(name = "Set-Cookie", description = "accessToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/"),
          @Header(name = "Set-Cookie", description = "refreshToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth")
      }
  )
  ResponseEntity<ApiResponse<Void>> register(RegisterRequest request);

  @Operation(
      summary = "이메일 찾기",
      description = "전화번호와 이름으로 가입된 이메일을 조회합니다."
  )
  ApiResponse<FindEmailResponse> findEmail(FindEmailRequest request);

  @Operation(
      summary = "비밀번호 재설정 1단계 - 본인확인",
      description = "이메일·이름·전화번호로 본인을 확인합니다. 일치하면 짧은 수명의 재설정 토큰을 "
          + "HttpOnly Cookie(passwordResetToken)로 발급하고, 없으면 404 를 반환합니다. "
          + "이어서 2단계(/password/reset)에서 새 비밀번호를 설정합니다."
  )
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      headers = @Header(name = "Set-Cookie",
          description = "passwordResetToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/password")
  )
  ResponseEntity<ApiResponse<Void>> verifyForPasswordReset(ResetPasswordVerifyRequest request);

  @Operation(
      summary = "비밀번호 재설정 2단계 - 비밀번호 변경",
      description = "1단계에서 발급된 재설정 토큰(Cookie)으로 새 비밀번호를 적용합니다. "
          + "토큰이 없거나 만료/위변조되었으면 401 을 반환합니다."
  )
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      headers = @Header(name = "Set-Cookie",
          description = "passwordResetToken=; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/password; Max-Age=0")
  )
  @Parameter(name = "passwordResetToken", in = ParameterIn.COOKIE, description = "1단계에서 발급된 재설정 토큰", required = true)
  ResponseEntity<ApiResponse<Void>> resetPassword(
      @Parameter(hidden = true) String resetToken,
      ResetPasswordConfirmRequest request);

  @Operation(
      summary = "토큰 재발급",
      description = "HttpOnly Cookie 의 refreshToken 으로 새 access/refresh 토큰을 발급(rotation)합니다. "
          + "무효화된(로그아웃/탈퇴) refreshToken 이면 401 을 반환합니다."
  )
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      headers = {
          @Header(name = "Set-Cookie", description = "accessToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/"),
          @Header(name = "Set-Cookie", description = "refreshToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth")
      }
  )
  @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, description = "리프레시 토큰", required = true)
  ResponseEntity<ApiResponse<Void>> refresh(@Parameter(hidden = true) String refreshToken);

  @Operation(
      summary = "로그아웃",
      description = "refreshToken 을 화이트리스트에서 무효화하고 access/refresh 쿠키를 삭제합니다."
  )
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      headers = {
          @Header(name = "Set-Cookie", description = "accessToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0"),
          @Header(name = "Set-Cookie", description = "refreshToken=; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth; Max-Age=0")
      }
  )
  @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, description = "리프레시 토큰", required = false)
  ResponseEntity<ApiResponse<Void>> logout(@Parameter(hidden = true) String refreshToken);

  @Operation(
      summary = "계정 정보 조회",
      description = "사이드바에 표시할 로그인 여부 및 계정 정보를 조회합니다. HttpOnly Cookie 의 accessToken 으로 인증합니다."
  )
  @Parameter(name = "accessToken", in = ParameterIn.COOKIE, description = "액세스 토큰", required = false)
  ApiResponse<AccountResponse> getAccount(@Parameter(hidden = true) Long memberId);
}
