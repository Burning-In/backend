package com.momentum.application;

import com.momentum.domain.service.StockCandleService;
import com.momentum.infrastructure.lsinvestment.LsStockChartClient;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockCandleFacade {

  private StockCandleService stockCandleService;
  private LsStockChartClient lsStockChartClient;

  public List<StockCandleInfo> create(StockCandleRequest stockCandleRequest) {
    StockChartInfoResponse response = lsStockChartClient.getDailyCandles(stockCandleRequest);
    return stockCandleService.create(stockCandleRequest.stockCode(), response).stream()
        .map(StockCandleInfo::from)
        .toList();
  }
}
