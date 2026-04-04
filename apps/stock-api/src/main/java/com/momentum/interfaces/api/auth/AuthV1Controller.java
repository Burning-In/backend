package com.momentum.interfaces.api.auth;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthV1Controller implements AuthV1ApiSpec {

    @PostMapping("/login")
    @Override
    public ApiResponse<LoginResponse> login(
        @RequestBody LoginRequest request
    ) {
        // TODO: AuthFacade 연결
        return ApiResponse.success(null);
    }

    @PostMapping("/register")
    @Override
    public ApiResponse<RegisterResponse> register(
        @RequestBody RegisterRequest request
    ) {
        // TODO: AuthFacade 연결
        return ApiResponse.success(null);
    }

    @PostMapping("/password/find")
    @Override
    public ApiResponse<FindPasswordResponse> findPassword(
        @RequestBody FindPasswordRequest request
    ) {
        // TODO: AuthFacade 연결
        return ApiResponse.success(null);
    }
}
