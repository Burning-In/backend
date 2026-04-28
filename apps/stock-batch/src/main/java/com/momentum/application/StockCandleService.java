package com.momentum.application;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.infrastructure.dto.StockChartInfoResponse.CandleResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockCandleService {

  private final StockRepository stockRepository;
  private final StockCandleRepository stockCandleRepository;

  @Transactional
  public List<StockDailyCandle> create(String stockCode, List<CandleResponse> candleResponses) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);
    List<StockDailyCandle> dailyCandles = candleResponses.stream()
        .map(candle -> fromCandle(stock, candle))
        .toList();

    return stockCandleRepository.saveAll(dailyCandles);
  }

  // DTO 받아서 던져주가,
  private StockDailyCandle fromCandle(Stock stock, CandleResponse candle) {
    return StockDailyCandle.create(
        stock,
        candle.date(),
        candle.openPrice(),
        candle.highPrice(),
        candle.lowPrice(),
        candle.closePrice(),
        candle.volume(),
        candle.priceChangeSign()
    );
  }
}
