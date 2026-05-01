package com.momentum.application;

import com.momentum.application.dto.StockCandleInfo;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.infrastructure.LsStockChartClient;
import com.momentum.infrastructure.dto.StockCandleRequest;
import com.momentum.infrastructure.dto.StockChartInfoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockCandleFacade {

  private final StockCandleService stockCandleService;
  private final LsStockChartClient lsStockChartClient;

  public List<StockDailyCandle> create(StockCandleRequest stockCandleRequest) {
    StockChartInfoResponse response = lsStockChartClient.getDailyCandles(stockCandleRequest);
    return stockCandleService.create(stockCandleRequest.stockCode(), response.candleResponses());
  }
}
