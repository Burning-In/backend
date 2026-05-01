package com.momentum.batch.job.regime;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockDailyRegimeService;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockRegimeItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockDailyRegimeService stockDailyRegimeService;
  private final StockCandleRepository stockCandleRepository;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public Stock process(Stock stock) throws Exception {
    LocalDate baseDate = LocalDate.parse(baseDateStr);
    StockDailyCandle candle = stockCandleRepository
        .findByStockAndDate(stock, baseDate)
        .orElseThrow(() -> new IllegalStateException("캔들 데이터 없음: " + stock.getId()));
    stockDailyRegimeService.finalizeDailyState(candle);
    return stock;
  }
}
