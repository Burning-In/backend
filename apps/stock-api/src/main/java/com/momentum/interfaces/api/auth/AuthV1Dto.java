package com.momentum.interfaces.api.auth;

public class AuthV1Dto {

    // ===================== Login =====================

    public record LoginRequest(
        String email,
        String password
    ) {}

    public record LoginResponse(
        String accessToken
    ) {}

    // ===================== Register =====================

    public record RegisterRequest(
        String email,
        String password,
        String passwordConfirm,
        String name,
        String phoneNumber
    ) {}

    public record RegisterResponse(
        String accessToken
    ) {}

    // 이메일 찾기는 (전화번호와 이름)
    // 비밀번호 찾기 -> 등록된 이메일로 뿌려주는걸로, 메일로

    // ===================== Find Password =====================

    public record FindPasswordRequest(
        String email,
        String phoneNumber
    ) {}

    public record FindPasswordResponse(
        String message
    ) {}

    // ===================== Refresh =====================

    public record RefreshResponse(
        String accessToken
    ) {}
}
