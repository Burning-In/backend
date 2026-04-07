package com.momentum.infrastructure.lsinvestment;

import com.momentum.application.dto.StockCandleRequest;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoRequest;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoResponse;
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
      StockCandleRequest stockCandleRequest
  ) {
    StockChartInfoRequest request = StockChartInfoRequest.daily(
        stockCandleRequest.stockCode(), stockCandleRequest.count(), stockCandleRequest.startDate(), stockCandleRequest.endDate()
    );

    StockChartInfoResponse response = restClient.post()
        .uri("/stock/chart")
        .header("authorization", "Bearer " + authToken)
        .header("tr_cd", "t8451")
        .header("tr_cont", "N")
        .body(request)
        .retrieve()
        .body(StockChartInfoResponse.class);

    if (response == null || response.candleResponses() == null) {
      throw new IllegalStateException("LS API 응답이 null입니다");
    }

    return response;
  }
}
