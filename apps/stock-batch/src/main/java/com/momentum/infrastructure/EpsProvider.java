package com.momentum.infrastructure;

import com.momentum.infrastructure.dto.FinancialRatioResponse;
import com.momentum.infrastructure.dto.FinancialRatioResponse.Output;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EpsProvider {

  private final RestClient kisRestClient;

  @Value("${kis-investment.app-key}")
  private String appKey;

  @Value("${kis-investment.app-secret}")
  private String appSecret;

  @Value("${kis-investment.auth-token}")
  private String authToken;

  public EpsProvider(@Qualifier("kisRestClient") RestClient kisRestClient) {
    this.kisRestClient = kisRestClient;
  }

  public List<Output> getQuarterlyEps(String stockCode) {
    FinancialRatioResponse response = kisRestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/uapi/domestic-stock/v1/finance/financial-ratio")
            .queryParam("FID_DIV_CLS_CODE", "1")
            .queryParam("fid_cond_mrkt_div_code", "J")
            .queryParam("fid_input_iscd", stockCode)
            .build())
        .header("authorization", "Bearer " + authToken)
        .header("appkey", appKey)
        .header("appsecret", appSecret)
        .header("tr_id", "FHKST66430300")
        .header("custtype", "P")
        .retrieve()
        .body(FinancialRatioResponse.class);

    if (response == null || response.output() == null) {
      throw new IllegalStateException("KIS 재무비율 응답이 null입니다");
    }

    return response.output();
  }
}
