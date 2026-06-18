package com.momentum.infrastructure.auth;

import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String ACCESS_TOKEN_COOKIE = "accessToken";

  private static final String ROLE_USER = "ROLE_USER";

  public static final String[] PROTECTED_PATTERNS = {
      "/api/v1/auth/account",
      "/api/v1/snapshots",
      "/api/v1/snapshots/**",
      "/api/v1/stocks/*/like",
      "/api/v1/stocks/likes"
  };

  private static final RequestMatcher PROTECTED_MATCHER = new OrRequestMatcher(
      Arrays.stream(PROTECTED_PATTERNS)
          .<RequestMatcher>map(AntPathRequestMatcher::new)
          .toList());

  private final JwtProvider jwtProvider;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      if (PROTECTED_MATCHER.matches(request)) {
        Long memberId = jwtProvider.resolveMemberId(resolveToken(request))
            .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "유효하지 않은 토큰입니다."));
        authenticate(memberId);
      }
      filterChain.doFilter(request, response);
    } catch (CoreException e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }

  private String resolveToken(HttpServletRequest request) {
    return findAccessToken(request)
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "로그인이 필요합니다."));
  }

  private Optional<String> findAccessToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(value -> value != null && !value.isBlank())
        .findFirst();
  }

  private void authenticate(Long memberId) {
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        memberId, null, List.of(new SimpleGrantedAuthority(ROLE_USER)));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
