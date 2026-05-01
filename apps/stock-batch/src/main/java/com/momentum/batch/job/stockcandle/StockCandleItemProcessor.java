package com.momentum.batch.job.stockcandle;

import com.momentum.application.StockCandleFacade;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.infrastructure.dto.StockCandleRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockCandleItemProcessor implements ItemProcessor<Stock, StockDailyCandle> {

  private final StockCandleFacade stockCandleFacade;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public StockDailyCandle process(Stock stock) throws Exception {
    LocalDate baseDate = LocalDate.parse(baseDateStr);
    StockCandleRequest stockCandleRequest = StockCandleRequest.of(stock.getCode(), baseDate);
    List<StockDailyCandle> stockDailyCandles = stockCandleFacade.create(stockCandleRequest);
    return stockDailyCandles.getFirst();
  }
}
