package com.momentum.interfaces.api.auth;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth V1 API", description = "인증/인가 관련 API 입니다.")
public interface AuthV1ApiSpec {

    @Operation(
        summary = "로그인",
        description = "ID와 비밀번호로 로그인합니다."
    )
    ApiResponse<LoginResponse> login(LoginRequest request);

    @Operation(
        summary = "회원가입",
        description = "이메일, 비밀번호, 전화번호, 닉네임으로 회원가입합니다."
    )
    ApiResponse<RegisterResponse> register(RegisterRequest request);

    @Operation(
        summary = "비밀번호 찾기",
        description = "이메일과 전화번호로 비밀번호 재설정 요청을 합니다."
    )
    ApiResponse<FindPasswordResponse> findPassword(FindPasswordRequest request);
}
