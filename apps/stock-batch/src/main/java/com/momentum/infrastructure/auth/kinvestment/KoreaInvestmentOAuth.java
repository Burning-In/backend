package com.momentum.infrastructure.auth.kinvestment;

import com.momentum.infrastructure.auth.kinvestment.dto.AuthTokenRequest;
import com.momentum.infrastructure.auth.kinvestment.dto.AuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KoreaInvestmentOAuth {

  private final RestClient restClient = RestClient.create();

  private static final String URL =
      "https://openapi.koreainvestment.com:9443/oauth2/tokenP";

  @Value("${kis-investment.app-key}")
  private String APP_KEY;

  @Value("${kis-investment.app-secret}")
  private String SECRET_KEY;

  public String getAccessToken() {
    try {
      AuthTokenRequest request = AuthTokenRequest.of(
          APP_KEY,
          SECRET_KEY
      );

      AuthTokenResponse response = restClient.post()
          .uri(URL)
          .header("content-type", "application/json")
          .body(request)
          .retrieve()
          .body(AuthTokenResponse.class);

      assert response != null;
      return response.accessToken();
    } catch (Exception e) {
      throw new RuntimeException("Access Token 발급 실패", e);
    }
  }
}
