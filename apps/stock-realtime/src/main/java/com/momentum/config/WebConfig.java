package com.momentum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트엔드(다른 포트)가 EventSource(withCredentials)로 SSE 를 구독하므로
 * credentials 허용 CORS 가 필요하다. 운영 도메인은 환경에 맞게 조정할 것.
 * (stock-api 의 CORS 정책과 동일한 기조)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns("http://localhost:*")
        .allowedMethods("GET", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
