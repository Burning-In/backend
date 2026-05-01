package com.momentum.domain.stockcandle;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
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
  public List<StockDailyCandle> create(String stockCode, List<StockCandleDto> stockCandles) {
    Stock stock = stockRepository.findByStockCode(stockCode)
        .orElseThrow(IllegalArgumentException::new);
    List<StockDailyCandle> dailyCandles = stockCandles.stream()
        .map(candle -> fromCandle(stock, candle))
        .toList();

    return stockCandleRepository.saveAll(dailyCandles);
  }

  private StockDailyCandle fromCandle(Stock stock, StockCandleDto candle) {
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
