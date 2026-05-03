package com.momentum.interfaces.api.auth;

public class AuthV1Dto {

    // ===================== Login =====================

    public record LoginRequest(
        String email,
        String password
    ) {}

    public record LoginResponse(
        Long userId,
        String nickname,
        String accessToken
    ) {}

    // ===================== Register =====================

    public record RegisterRequest(
        String email,
        String password,
        String passwordConfirm,
        String phoneNumber,
        String nickname,
        boolean agreeToTerms,
        boolean agreeToPrivacyPolicy
    ) {}

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
