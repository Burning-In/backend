package com.momentum.interfaces.api.stock;

import com.momentum.application.StockChartService;
import com.momentum.domain.ma.StockMovingAveragePeriod;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.DailyCandleResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.MovingAverageResponse;
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
public class StockChartV1Controller implements StockChartV1ApiSpec {

  private final StockChartService stockChartService;

  @GetMapping("/chart/daily")
  @Override
  public ApiResponse<DailyCandleResponse> getDailyCandle(
      @PathVariable String stockCode,
      @RequestParam LocalDate from,
      @RequestParam LocalDateTime to
  ) {
    return ApiResponse.success(stockChartService.getDailyCandles(stockCode, from, to.toLocalDate()));
  }

  @GetMapping("/chart/moving-averages")
  @Override
  public ApiResponse<MovingAverageResponse> getMovingAverages(
      @PathVariable String stockCode,
      @RequestParam StockMovingAveragePeriod period,
      @RequestParam LocalDate from,
      @RequestParam LocalDateTime to
  ) {
    return ApiResponse.success(stockChartService.getMovingAverages(stockCode, period, from, to.toLocalDate()));
  }

  @GetMapping("/chart/bases")
  @Override
  public ApiResponse<BaseListResponse> getBases(
      @PathVariable String stockCode,
      @RequestParam LocalDate from,
      @RequestParam LocalDateTime to
  ) {
    return ApiResponse.success(stockChartService.getBases(stockCode, from, to.toLocalDate()));
  }
}
