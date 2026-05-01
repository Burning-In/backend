package com.momentum.application;

import com.momentum.domain.stockcandle.StockCandleDto;
import com.momentum.domain.stockcandle.StockCandleService;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.infrastructure.LsStockChartClient;
import com.momentum.infrastructure.dto.StockCandleRequest;
import com.momentum.infrastructure.dto.StockChartInfoResponse;
import com.momentum.infrastructure.dto.StockChartInfoResponse.CandleResponse;
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
    return stockCandleService.create(stockCandleRequest.stockCode(), fromChartInfo(response.candleResponses()));
  }

  private List<StockCandleDto> fromChartInfo(List<CandleResponse> candleResponse) {
    return candleResponse.stream()
        .map(candle -> new StockCandleDto(
            candle.date(),
            candle.openPrice(),
            candle.highPrice(),
            candle.lowPrice(),
            candle.closePrice(),
            candle.volume(),
            candle.tradingValue(),
            candle.priceChangeSign()
        ))
        .toList();
  }
}
