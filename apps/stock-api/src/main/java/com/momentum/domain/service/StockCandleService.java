package com.momentum.domain.service;

import com.momentum.domain.entity.stock.Stock;
import com.momentum.domain.entity.stock.StockDailyCandle;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoResponse;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoResponse.CandleResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockCandleService{

  private final StockRepository stockRepository;
  private final StockCandleRepository stockCandleRepository;

  @Transactional
  public List<StockDailyCandle> create(String stockCode, StockChartInfoResponse stockChartInfoResponse) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);

    List<StockDailyCandle> dailyCandles = stockChartInfoResponse.candleResponses()
        .stream()
        .map(candle -> fromCandle(stock, candle))
        .toList();

    return stockCandleRepository.saveAll(dailyCandles);
  }

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
