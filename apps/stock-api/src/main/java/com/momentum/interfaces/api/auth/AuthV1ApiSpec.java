package com.momentum.interfaces.api.auth;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.RefreshResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth V1 API", description = "인증/인가 관련 API 입니다.")
public interface AuthV1ApiSpec {

    @Operation(
        summary = "CSRF 토큰 발급",
        description = "페이지 진입 시 호출합니다. csrfToken을 Cookie로 발급합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        headers = @Header(name = "Set-Cookie", description = "csrfToken=<token>; Path=/")
    )
    ApiResponse<Void> csrf();

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다. refreshToken은 HttpOnly Cookie로 내려갑니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        headers = @Header(name = "Set-Cookie", description = "refreshToken=<token>; HttpOnly; Path=/api/v1/auth")
    )
    @Parameter(name = "X-CSRF-Token", in = ParameterIn.HEADER, description = "CSRF 토큰", required = true)
    ApiResponse<LoginResponse> login(LoginRequest request);

    @Operation(
        summary = "회원가입",
        description = "이메일, 비밀번호, 전화번호, 닉네임으로 회원가입합니다."
    )
    ApiResponse<RegisterResponse> register(RegisterRequest request);

    @Operation(
        summary = "이메일 찾기",
        description = "전화번호와 이름으로 가입된 이메일을 조회합니다."
    )
    ApiResponse<FindEmailResponse> findEmail(FindEmailRequest request);

    @Operation(
        summary = "비밀번호 찾기",
        description = "이메일로 비밀번호 재설정 링크를 발송합니다."
    )
    ApiResponse<Void> findPassword(FindPasswordRequest request);

    @Operation(
        summary = "토큰 재발급",
        description = "HttpOnly Cookie의 refreshToken으로 새로운 accessToken을 발급합니다."
    )
    @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, description = "리프레시 토큰", required = true)
    @Parameter(name = "X-CSRF-Token", in = ParameterIn.HEADER, description = "CSRF 토큰", required = true)
    ApiResponse<RefreshResponse> refresh();

    @Operation(
        summary = "로그아웃",
        description = "HttpOnly Cookie의 refreshToken을 만료시킵니다. accessToken은 TTL까지 자연 만료됩니다."
    )
    @Parameter(name = "X-CSRF-Token", in = ParameterIn.HEADER, description = "CSRF 토큰", required = true)
    ApiResponse<Void> logout();
}
