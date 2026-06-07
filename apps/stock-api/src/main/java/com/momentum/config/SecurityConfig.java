package com.momentum.config;

import com.momentum.infrastructure.auth.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      CsrfTokenRepository csrfTokenRepository) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            // 명세상 CSRF 토큰이 필요한 엔드포인트는 login/refresh/logout 뿐이다.
            .requireCsrfProtectionMatcher(csrfProtectedMatcher()))
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        // 아직 도메인 엔드포인트는 인증을 강제하지 않는다(기존 동작 유지). 토큰이 있으면 인증 정보만 채운다.
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** 쿠키 이름 {@code csrfToken}, 헤더 이름 {@code X-CSRF-Token} 으로 명세에 맞춘다. */
  @Bean
  @SuppressWarnings("deprecation") // setCookieName 은 deprecated 이나 customizer 로는 쿠키 이름을 바꿀 수 없다.
  public CsrfTokenRepository csrfTokenRepository() {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setHeaderName("X-CSRF-Token");
    repository.setCookieName("csrfToken");
    repository.setCookieCustomizer(cookie -> cookie.path("/"));
    return repository;
  }

  private RequestMatcher csrfProtectedMatcher() {
    return new OrRequestMatcher(
        new AntPathRequestMatcher("/api/v1/auth/login", "POST"),
        new AntPathRequestMatcher("/api/v1/auth/refresh", "POST"),
        new AntPathRequestMatcher("/api/v1/auth/logout", "POST"));
  }

  /** 프론트엔드가 쿠키(refreshToken/csrfToken)를 주고받으므로 credentials 를 허용한다. 운영 도메인은 환경에 맞게 조정 필요. */
  private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("http://localhost:*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
