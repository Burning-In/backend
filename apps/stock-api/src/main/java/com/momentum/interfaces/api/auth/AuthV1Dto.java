package com.momentum.interfaces.api.auth;

public class AuthV1Dto {

  // ===================== Login =====================

  public record LoginRequest(
      String email,
      String password
  ) {

  }

  // ===================== Register =====================

  public record RegisterRequest(
      String email,
      String password,
      String name,
      String phoneNumber
  ) {

  }

  // ===================== Find Email =====================

  public record FindEmailRequest(
      String phoneNumber,
      String name
  ) {

  }

  public record FindEmailResponse(
      String email
  ) {

  }

  // ===================== Reset Password =====================

  public record ResetPasswordVerifyRequest(
      String email,
      String name,
      String phoneNumber
  ) {

  }

  public record ResetPasswordConfirmRequest(
      String newPassword
  ) {

  }

  // ===================== Account Area =====================

  public record AccountResponse(
      boolean isLoggedIn,
      Long userId,
      String nickname
  ) {}
}
