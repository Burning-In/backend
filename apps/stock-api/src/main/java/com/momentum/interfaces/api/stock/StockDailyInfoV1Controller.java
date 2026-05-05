package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockDailyInfoV1Dto.DailyCandleResponse;
import com.momentum.interfaces.api.stock.StockDailyInfoV1Dto.IntradayDailyCandleResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stocks/{stockCode}")
public class StockDailyInfoV1Controller implements StockDailyInfoV1ApiSpec {

  @GetMapping("/chart/daily")
  @Override
  public ApiResponse<DailyCandleResponse> getDailyCandle(
      @PathVariable String stockCode,
      @RequestParam LocalDate from,
      @RequestParam LocalDate to
  ) {
    // TODO: StockDailyInfoFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/chart/intraday")
  @Override
  public ApiResponse<IntradayDailyCandleResponse> getIntradayDailyCandle(
      @PathVariable String stockCode,
      @RequestParam LocalDate tradeDate,
      @RequestParam LocalDateTime to
  ) {
    // TODO: StockDailyInfoFacade 연결
    return ApiResponse.success(null);
  }
}
