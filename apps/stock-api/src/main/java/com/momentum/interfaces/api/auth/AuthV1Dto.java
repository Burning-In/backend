package com.momentum.interfaces.api.auth;

public class AuthV1Dto {

  // ===================== Login =====================

  public record LoginRequest(
      String email,
      String password
  ) {

  }

  public record LoginResponse(
      String accessToken
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

  public record RegisterResponse(
      String accessToken
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

  // ===================== Find Password =====================

  public record FindPasswordRequest(
      String email,
      String name
  ) {

  }


  // ===================== Refresh =====================

  public record RefreshResponse(
      String accessToken
  ) {

  }

  // ===================== Account Area =====================

  public record AccountResponse(
      boolean isLoggedIn,
      Long userId,
      String nickname
  ) {}
}
