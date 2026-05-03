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
        String phoneNumber,
        String nickname,
        boolean agreeToTerms, // 약관이 있었나?
        boolean agreeToPrivacyPolicy
    ) {}

//  1. 회원가입 → 바로 로그인 처리
//  2. 회원가입 → 로그인 페이지로 이동
    public record RegisterResponse(
        Long userId,
        String email,
        String nickname
    ) {}

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
