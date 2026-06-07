package com.momentum.application;

import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import com.momentum.infrastructure.auth.JwtProvider;
import com.momentum.interfaces.api.auth.AuthV1Dto.AccountResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindEmailResponse;
import com.momentum.interfaces.api.auth.AuthV1Dto.FindPasswordRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.LoginRequest;
import com.momentum.interfaces.api.auth.AuthV1Dto.RegisterRequest;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
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
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  @Transactional
  public AuthTokens register(RegisterRequest request) {
    memberRepository.findByEmail(request.email())
        .ifPresent(member -> {
          throw new CoreException(ErrorType.CONFLICT, "이미 가입된 이메일입니다: " + request.email());
        });

    Member member = memberRepository.save(Member.create(
        request.email(),
        passwordEncoder.encode(request.password()),
        request.name(), // 별도 닉네임 입력이 없어 이름을 기본 닉네임으로 사용한다.
        request.name(),
        request.phoneNumber()));

    return issueTokens(member.getId());
  }

  @Transactional(readOnly = true)
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
  public void findPassword(FindPasswordRequest request) {
    // 계정 존재 여부를 노출하지 않기 위해, 회원이 있을 때만 재설정 링크를 발송하고 응답은 항상 동일하게 성공 처리한다.
    memberRepository.findByEmailAndName(request.email(), request.name())
        .ifPresent(member -> {
          // TODO: 비밀번호 재설정 토큰 생성 후 이메일 발송 (메일 인프라 연동 필요)
          log.info("비밀번호 재설정 링크 발송 요청: memberId={}", member.getId());
        });
  }

  /**
   * HttpOnly 쿠키의 refreshToken 으로 새 accessToken 을 발급한다.
   */
  @Transactional(readOnly = true)
  public String refreshAccessToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
    }
    Long memberId = jwtProvider.resolveMemberId(refreshToken)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));

    memberRepository.findById(memberId)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "존재하지 않는 회원입니다."));

    return jwtProvider.createAccessToken(memberId);
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
    return new AuthTokens(
        jwtProvider.createAccessToken(memberId),
        jwtProvider.createRefreshToken(memberId));
  }
}
