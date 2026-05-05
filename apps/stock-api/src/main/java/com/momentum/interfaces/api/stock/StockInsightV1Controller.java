package com.momentum.interfaces.api.stock;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stocks/{stockCode}/insight")
public class StockInsightV1Controller implements StockInsightV1ApiSpec {

  @GetMapping("/regime")
  @Override
  public ApiResponse<StockRegimeResponse> getRegime(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/moving-average")
  @Override
  public ApiResponse<MovingAverageResponse> getMovingAverage(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/momentum")
  @Override
  public ApiResponse<MomentumResponse> getMomentum(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/volume")
  @Override
  public ApiResponse<VolumeResponse> getVolume(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/fip")
  @Override
  public ApiResponse<FrogInPanResponse> getFrogInPan(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/rs")
  @Override
  public ApiResponse<RsResponse> getRs(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }

  @GetMapping("/eps")
  @Override
  public ApiResponse<EpsResponse> getEps(
      @PathVariable String stockCode
  ) {
    // TODO: StockInsightFacade 연결
    return ApiResponse.success(null);
  }
}
