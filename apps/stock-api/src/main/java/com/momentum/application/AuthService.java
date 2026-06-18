package com.momentum.application;

import com.momentum.config.JwtProperties;
import com.momentum.domain.auth.RefreshToken;
import com.momentum.domain.auth.RefreshTokenRepository;
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
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;

  @Transactional
  public AuthTokens register(RegisterRequest request) {
    memberRepository.findByEmail(request.email())
        .ifPresent(member -> {
          throw new CoreException(ErrorType.CONFLICT, "이미 가입된 이메일입니다: " + request.email());
        });

    Member member = memberRepository.save(Member.create(
        request.email(),
        passwordEncoder.encode(request.password()),
        request.name(),
        request.name(),
        request.phoneNumber()));

    return issueTokens(member.getId());
  }

  @Transactional
  public AuthTokens login(LoginRequest request) {
    Member member = memberRepository.findByEmail(request.email())
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

    if (!passwordEncoder.matches(request.password(), member.getPassword())) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    return issueTokens(member.getId());
  }

  @Transactional(readOnly = true)
  public FindEmailResponse findEmail(FindEmailRequest request) {
    Member member = memberRepository.findByPhoneNumberAndName(request.phoneNumber(), request.name())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "일치하는 회원을 찾을 수 없습니다."));
    return new FindEmailResponse(member.getEmail());
  }

  @Transactional(readOnly = true)
  public String verifyForPasswordReset(ResetPasswordVerifyRequest request) {
    Member member = memberRepository
        .findByEmailAndNameAndPhoneNumber(request.email(), request.name(), request.phoneNumber())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "일치하는 회원을 찾을 수 없습니다."));
    return jwtProvider.createPasswordResetToken(member.getId(), Instant.now());
  }

  @Transactional
  public void confirmPasswordReset(String resetToken, ResetPasswordConfirmRequest request) {
    if (resetToken == null || resetToken.isBlank()) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "본인확인이 필요합니다.");
    }
    if (request.newPassword() == null || request.newPassword().isBlank()) {
      throw new CoreException(ErrorType.BAD_REQUEST, "새 비밀번호를 입력해주세요.");
    }
    Long memberId = jwtProvider.resolvePasswordResetMemberId(resetToken)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "유효하지 않거나 만료된 본인확인 정보입니다."));

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다."));

    member.changePassword(passwordEncoder.encode(request.newPassword()));
    memberRepository.save(member);
  }

  @Transactional
  public AuthTokens reissueRefreshToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
    }
    Long memberId = jwtProvider.resolveMemberId(refreshToken)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));
    memberRepository.findById(memberId)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "존재하지 않는 회원입니다."));

    RefreshToken savedRefreshToken = refreshTokenRepository.findActiveByToken(refreshToken)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "무효화된 리프레시 토큰입니다."));
    savedRefreshToken.delete();

    return issueTokens(memberId);
  }

  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }
    refreshTokenRepository.findActiveByToken(refreshToken)
        .ifPresent(RefreshToken::delete);
  }

  @Transactional(readOnly = true)
  public AccountResponse getAccount(Long memberId) {
    if (memberId == null) {
      return new AccountResponse(false, null, null);
    }
    return memberRepository.findById(memberId)
        .map(member -> new AccountResponse(true, member.getId(), member.getNickname()))
        .orElseGet(() -> new AccountResponse(false, null, null));
  }

  private AuthTokens issueTokens(Long memberId) {
    Instant now = Instant.now();
    String accessToken = jwtProvider.createAccessToken(memberId, now);
    String refreshToken = jwtProvider.createRefreshToken(memberId, now);
    Instant expiresAt = now.plus(jwtProperties.refreshTokenValidity());
    refreshTokenRepository.save(RefreshToken.issue(memberId, refreshToken, expiresAt));
    return new AuthTokens(accessToken, refreshToken);
  }
}
