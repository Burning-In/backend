package com.momentum.infrastructure.lsinvestment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .baseUrl("https://openapi.ls-sec.co.kr:8080")
        .defaultHeader("type", "application/json; charset=utf-8")
        .build();
  }
}
