package com.momentum.interfaces.api.stock;

import com.momentum.application.insight.EpsInsightService;
import com.momentum.application.insight.FrogInPanInsightService;
import com.momentum.application.insight.MomentumInsightService;
import com.momentum.application.insight.MovingAverageInsightService;
import com.momentum.application.insight.RegimeInsightService;
import com.momentum.application.insight.RsInsightService;
import com.momentum.application.insight.VolumeInsightService;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stocks/{stockCode}/insight")
public class StockInsightV1Controller implements StockInsightV1ApiSpec {

  private final StockRepository stockRepository;
  private final RegimeInsightService regimeInsightService;
  private final MovingAverageInsightService movingAverageInsightService;
  private final MomentumInsightService momentumInsightService;
  private final FrogInPanInsightService frogInPanInsightService;
  private final VolumeInsightService volumeInsightService;
  private final RsInsightService rsInsightService;
  private final EpsInsightService epsInsightService;

  @GetMapping("/regime")
  @Override
  public ApiResponse<StockRegimeResponse> getRegime(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(regimeInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  @GetMapping("/moving-average")
  @Override
  public ApiResponse<MovingAverageResponse> getMovingAverage(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(movingAverageInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  @GetMapping("/momentum")
  @Override
  public ApiResponse<MomentumResponse> getMomentum(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(momentumInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  @GetMapping("/volume")
  @Override
  public ApiResponse<VolumeResponse> getVolume(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(volumeInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  @GetMapping("/fip")
  @Override
  public ApiResponse<FrogInPanResponse> getFrogInPan(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(frogInPanInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  @GetMapping("/rs")
  @Override
  public ApiResponse<RsResponse> getRs(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(rsInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  @GetMapping("/eps")
  @Override
  public ApiResponse<EpsResponse> getEps(
      @PathVariable String stockCode,
      @RequestParam LocalDateTime at
  ) {
    return ApiResponse.success(epsInsightService.query(findStock(stockCode), at.toLocalDate()));
  }

  private Stock findStock(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode));
  }
}
