package com.momentum.infrastructure.lsinvestment;

import com.momentum.infrastructure.lsinvestment.dto.stocktick.LsTokenRequest;
import com.momentum.infrastructure.lsinvestment.dto.stocktick.LsTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class LsAuthTokenProvider {

  private final RestClient lsWebClient;

  @Value("${ls-investment.app-key}")
  private String appKey;

  @Value("${ls-investment.secret-key}")
  private String appSecret;

  public LsTokenResponse issueToken() {
    LsTokenRequest request = LsTokenRequest.create(appKey, appSecret);

    return lsWebClient.post()
        .uri("/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(request.toFormData())
        .retrieve()
        .body(LsTokenResponse.class);
  }
}
