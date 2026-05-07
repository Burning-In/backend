package com.momentum.infrastructure;

import com.momentum.infrastructure.dto.KospiIndexRequest;
import com.momentum.infrastructure.dto.KospiIndexResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LsKospiProvider {

  private final RestClient restClient;

  @Value("${ls-investment.auth-token}")
  private String authToken;

  public long getAfterMarketKospi() {
    KospiIndexResponse response = restClient.post()
        .uri("/indtp/market-data")
        .header("authorization", "Bearer " + authToken)
        .header("tr_cd", "t1485")
        .header("tr_cont", "N")
        .body(KospiIndexRequest.afterMarket())
        .retrieve()
        .body(KospiIndexResponse.class);

     if (response == null || response.outBlock() == null) {
      throw new IllegalStateException("KOSPI 지수 응답이 null입니다");
    }

    return (long) Double.parseDouble(response.outBlock().pricejisu());
  }
}
