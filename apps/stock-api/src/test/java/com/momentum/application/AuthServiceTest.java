package com.momentum.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import com.momentum.infrastructure.auth.JwtProvider;
import com.momentum.interfaces.api.auth.AuthV1Dto.AccountResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.ResetPasswordConfirmRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.ResetPasswordVerifyRequest;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class AuthServiceTest {

  @Autowired
  private AuthService authService;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private JwtProvider jwtProvider;

  @Test
  @DisplayName("회원가입 시 비밀번호는 암호화되어 저장되고 access/refresh 토큰이 발급된다")
  void registerEncodesPasswordAndIssuesTokens() {
    AuthTokens tokens = authService.register(register("a@momentum.com", "pw1234"));

    Member member = memberRepository.findByEmail("a@momentum.com").orElseThrow();
    assertThat(member.getPassword()).isNotEqualTo("pw1234");
    assertThat(passwordEncoder.matches("pw1234", member.getPassword())).isTrue();
    assertThat(jwtProvider.resolveMemberId(tokens.accessToken())).contains(member.getId());
    assertThat(jwtProvider.resolveMemberId(tokens.refreshToken())).contains(member.getId());
  }

  @Test
  @DisplayName("이미 가입된 이메일로 회원가입하면 CONFLICT")
  void registerThrowsWhenEmailExists() {
    authService.register(register("dup@momentum.com", "pw1234"));

    assertThatThrownBy(() -> authService.register(register("dup@momentum.com", "other")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.CONFLICT);
  }

  @Test
  @DisplayName("올바른 자격증명으로 로그인하면 회원의 토큰이 발급된다")
  void loginSucceedsWithValidCredentials() {
    Member member = memberRepository.findByEmail("login@momentum.com")
        .orElseGet(() -> {
          authService.register(register("login@momentum.com", "pw1234"));
          return memberRepository.findByEmail("login@momentum.com").orElseThrow();
        });

    AuthTokens tokens = authService.login(new LoginRequest("login@momentum.com", "pw1234"));

    assertThat(jwtProvider.resolveMemberId(tokens.accessToken())).contains(member.getId());
  }

  @Test
  @DisplayName("비밀번호가 틀리면 UNAUTHORIZED")
  void loginThrowsWhenPasswordWrong() {
    authService.register(register("wrongpw@momentum.com", "pw1234"));

    assertThatThrownBy(() -> authService.login(new LoginRequest("wrongpw@momentum.com", "nope")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 로그인하면 UNAUTHORIZED")
  void loginThrowsWhenEmailNotFound() {
    assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@momentum.com", "pw1234")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("전화번호와 이름으로 가입된 이메일을 찾는다")
  void findEmailReturnsRegisteredEmail() {
    authService.register(new RegisterRequest("find@momentum.com", "pw1234", "홍길동", "01011112222"));

    FindEmailResponse response = authService.findEmail(new FindEmailRequest("01011112222", "홍길동"));

    assertThat(response.email()).isEqualTo("find@momentum.com");
  }

  @Test
  @DisplayName("일치하는 회원이 없으면 이메일 찾기는 NOT_FOUND")
  void findEmailThrowsWhenNotFound() {
    assertThatThrownBy(() -> authService.findEmail(new FindEmailRequest("00000000000", "없는사람")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.NOT_FOUND);
  }

  @Test
  @DisplayName("1단계 본인확인 통과 후 2단계에서 비밀번호가 변경된다")
  void passwordResetTwoStepChangesPassword() {
    authService.register(new RegisterRequest("reset@momentum.com", "oldpw", "홍길동", "01099998888"));

    String resetToken = authService.verifyForPasswordReset(
        new ResetPasswordVerifyRequest("reset@momentum.com", "홍길동", "01099998888"));
    authService.confirmPasswordReset(resetToken, new ResetPasswordConfirmRequest("newpw1234"));

    Member member = memberRepository.findByEmail("reset@momentum.com").orElseThrow();
    assertThat(passwordEncoder.matches("newpw1234", member.getPassword())).isTrue();
    assertThat(passwordEncoder.matches("oldpw", member.getPassword())).isFalse();
  }

  @Test
  @DisplayName("1단계 본인확인 정보가 일치하지 않으면 NOT_FOUND")
  void passwordResetVerifyThrowsWhenIdentityMismatch() {
    authService.register(new RegisterRequest("reset2@momentum.com", "oldpw", "홍길동", "01099998888"));

    assertThatThrownBy(() -> authService.verifyForPasswordReset(
        new ResetPasswordVerifyRequest("reset2@momentum.com", "다른이름", "01099998888")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.NOT_FOUND);
  }

  @Test
  @DisplayName("2단계에서 새 비밀번호가 비어있으면 BAD_REQUEST")
  void passwordResetConfirmThrowsWhenNewPasswordBlank() {
    authService.register(new RegisterRequest("reset3@momentum.com", "oldpw", "홍길동", "01099998888"));
    String resetToken = authService.verifyForPasswordReset(
        new ResetPasswordVerifyRequest("reset3@momentum.com", "홍길동", "01099998888"));

    assertThatThrownBy(() ->
        authService.confirmPasswordReset(resetToken, new ResetPasswordConfirmRequest("  ")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.BAD_REQUEST);
  }

  @Test
  @DisplayName("2단계에서 재설정 토큰이 없으면 UNAUTHORIZED")
  void passwordResetConfirmThrowsWhenTokenMissing() {
    assertThatThrownBy(() ->
        authService.confirmPasswordReset(null, new ResetPasswordConfirmRequest("newpw1234")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("재설정 토큰 대신 일반 accessToken을 쓰면(용도 불일치) UNAUTHORIZED")
  void passwordResetConfirmRejectsNonResetToken() {
    authService.register(new RegisterRequest("reset4@momentum.com", "oldpw", "홍길동", "01099998888"));
    Member member = memberRepository.findByEmail("reset4@momentum.com").orElseThrow();
    String accessToken = jwtProvider.createAccessToken(member.getId(), Instant.now());

    assertThatThrownBy(() ->
        authService.confirmPasswordReset(accessToken, new ResetPasswordConfirmRequest("newpw1234")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("만료된 재설정 토큰이면 UNAUTHORIZED")
  void passwordResetConfirmThrowsWhenTokenExpired() {
    authService.register(new RegisterRequest("reset5@momentum.com", "oldpw", "홍길동", "01099998888"));
    Member member = memberRepository.findByEmail("reset5@momentum.com").orElseThrow();
    // 재설정 토큰 유효기간(10분)보다 더 과거에 발급 → 이미 만료
    String expired = jwtProvider.createPasswordResetToken(
        member.getId(), Instant.now().minus(Duration.ofMinutes(11)));

    assertThatThrownBy(() ->
        authService.confirmPasswordReset(expired, new ResetPasswordConfirmRequest("newpw1234")))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("유효한 refreshToken으로 새 access/refresh 토큰 쌍을 발급한다")
  void refreshIssuesNewTokens() {
    AuthTokens tokens = authService.register(register("refresh@momentum.com", "pw1234"));
    Long memberId = jwtProvider.resolveMemberId(tokens.refreshToken()).orElseThrow();

    AuthTokens refreshed = authService.reissueRefreshToken(tokens.refreshToken());

    assertThat(jwtProvider.resolveMemberId(refreshed.accessToken())).contains(memberId);
    assertThat(jwtProvider.resolveMemberId(refreshed.refreshToken())).contains(memberId);
  }

  @Test
  @DisplayName("회전(rotation) 후 기존 refreshToken은 무효화되어 재사용할 수 없다")
  void refreshRotationInvalidatesOldToken() {
    AuthTokens tokens = authService.register(register("rotate@momentum.com", "pw1234"));

    authService.reissueRefreshToken(tokens.refreshToken());

    assertThatThrownBy(() -> authService.reissueRefreshToken(tokens.refreshToken()))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("로그아웃으로 무효화된 refreshToken이면 UNAUTHORIZED")
  void refreshThrowsWhenTokenRevoked() {
    AuthTokens tokens = authService.register(register("revoked@momentum.com", "pw1234"));
    authService.logout(tokens.refreshToken());

    assertThatThrownBy(() -> authService.reissueRefreshToken(tokens.refreshToken()))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("화이트리스트에 없는(저장되지 않은) refreshToken은 서명이 유효해도 UNAUTHORIZED")
  void refreshThrowsWhenTokenNotWhitelisted() {
    authService.register(register("nowhitelist@momentum.com", "pw1234"));
    Member member = memberRepository.findByEmail("nowhitelist@momentum.com").orElseThrow();
    String unsavedRefresh = jwtProvider.createRefreshToken(member.getId(), Instant.now());

    assertThatThrownBy(() -> authService.reissueRefreshToken(unsavedRefresh))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("유효하지 않은 refreshToken이면 UNAUTHORIZED")
  void refreshThrowsWhenTokenInvalid() {
    assertThatThrownBy(() -> authService.reissueRefreshToken("invalid.token.value"))
        .isInstanceOf(CoreException.class)
        .extracting(e -> ((CoreException) e).getErrorType())
        .isEqualTo(ErrorType.UNAUTHORIZED);
  }

  @Test
  @DisplayName("refreshToken이 없으면 UNAUTHORIZED")
  void refreshThrowsWhenTokenMissing() {
    assertThatThrownBy(() -> authService.reissueRefreshToken(null))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("로그인하지 않은 경우 계정 조회는 isLoggedIn=false")
  void getAccountReturnsLoggedOutWhenNull() {
    AccountResponse response = authService.getAccount(null);

    assertThat(response.isLoggedIn()).isFalse();
    assertThat(response.userId()).isNull();
  }

  @Test
  @DisplayName("회원 ID가 있으면 계정 정보를 반환한다")
  void getAccountReturnsMemberInfo() {
    authService.register(register("account@momentum.com", "pw1234"));
    Member member = memberRepository.findByEmail("account@momentum.com").orElseThrow();

    AccountResponse response = authService.getAccount(member.getId());

    assertThat(response.isLoggedIn()).isTrue();
    assertThat(response.userId()).isEqualTo(member.getId());
    assertThat(response.nickname()).isEqualTo(member.getNickname());
  }

  private RegisterRequest register(String email, String password) {
    return new RegisterRequest(email, password, "이름", "01000000000");
  }
}
