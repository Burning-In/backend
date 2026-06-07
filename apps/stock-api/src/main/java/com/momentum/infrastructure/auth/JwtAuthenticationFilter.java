package com.momentum.infrastructure.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization: Bearer accessToken 을 검증해 SecurityContext 에 인증 정보를 채운다.
 * 토큰이 없거나 유효하지 않으면 인증을 채우지 않고 그대로 통과시킨다(선택적 인증).
 * principal 에는 회원 ID(Long) 를 담는다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    resolveToken(request)
        .flatMap(jwtProvider::resolveMemberId)
        .ifPresent(memberId -> authenticate(memberId, request));
    filterChain.doFilter(request, response);
  }

  private java.util.Optional<String> resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return java.util.Optional.of(header.substring(BEARER_PREFIX.length()));
    }
    return java.util.Optional.empty();
  }

  private void authenticate(Long memberId, HttpServletRequest request) {
    var authentication = new UsernamePasswordAuthenticationToken(
        memberId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
