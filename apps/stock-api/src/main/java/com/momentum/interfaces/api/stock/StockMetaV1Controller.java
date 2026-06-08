package com.momentum.interfaces.api.stock;

import com.momentum.application.StockQueryService;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.stock.StockMetaV1Dto.StockMetaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stocks")
public class StockMetaV1Controller implements StockMetaV1ApiSpec {

  private final StockQueryService stockQueryService;

  @GetMapping("/{stockCode}")
  @Override
  public ApiResponse<StockMetaResponse> getStockMeta(
      @PathVariable String stockCode
  ) {
    return ApiResponse.success(
        StockMetaResponse.from(stockQueryService.getByCode(stockCode)));
  }
}
