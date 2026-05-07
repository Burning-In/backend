package com.momentum.infrastructure;

import com.momentum.infrastructure.dto.KisKospiIndexResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LsKospiProvider {

  private final RestClient kisRestClient;

  @Value("${kis-investment.app-key}")
  private String appKey;

  @Value("${kis-investment.app-secret}")
  private String appSecret;

  @Value("${kis-investment.auth-token}")
  private String authToken;

  public LsKospiProvider(@Qualifier("kisRestClient") RestClient kisRestClient) {
    this.kisRestClient = kisRestClient;
  }

  public long getAfterMarketKospi(LocalDate date) {
    String today = date.format(DateTimeFormatter.BASIC_ISO_DATE);

    KisKospiIndexResponse response = kisRestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/uapi/domestic-stock/v1/quotations/inquire-index-daily-price")
            .queryParam("FID_PERIOD_DIV_CODE", "D")
            .queryParam("FID_COND_MRKT_DIV_CODE", "U")
            .queryParam("FID_INPUT_ISCD", "0001")
            .queryParam("FID_INPUT_DATE_1", today)
            .build())
        .header("authorization", "Bearer " + authToken)
        .header("appkey", appKey)
        .header("appsecret", appSecret)
        .header("tr_id", "FHPUP02120000")
        .header("custtype", "P")
        .retrieve()
        .body(KisKospiIndexResponse.class);

    if (response == null || response.output1() == null) {
      throw new IllegalStateException("KOSPI 지수 응답이 null입니다");
    }

    return (long) Double.parseDouble(response.output1().bstpNmixPrpr());
  }
}
