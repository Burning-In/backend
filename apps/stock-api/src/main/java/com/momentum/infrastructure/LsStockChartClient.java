package com.momentum.infrastructure;

import com.momentum.infrastructure.dto.StockChartInfoRequest;
import com.momentum.infrastructure.dto.StockChartInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LsStockChartClient {

  private final RestClient restClient;

  @Value("${ls-investment.auth-token}")
  private String authToken;

  public StockChartInfoResponse getDailyCandles(
      String stockCode,
      int count,
      String startDate,
      String endDate
  ) {
    StockChartInfoRequest request = StockChartInfoRequest.daily(
        stockCode, count, startDate, endDate
    );

    StockChartInfoResponse response = restClient.post()
        .uri("/stock/chart")
        .header("authorization", "Bearer " + authToken)
        .header("tr_cd", "t8451")
        .header("tr_cont", "N")
        .body(request)
        .retrieve()
        .body(StockChartInfoResponse.class);

    if (response == null || response.candles() == null) {
      throw new IllegalStateException("LS API 응답이 null입니다");
    }

    return response;
  }
}
