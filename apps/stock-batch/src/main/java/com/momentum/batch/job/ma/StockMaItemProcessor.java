package com.momentum.batch.job.ma;

import com.momentum.domain.movingaverage.StockMovingAverageService;
import com.momentum.domain.stock.Stock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockMaItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockMovingAverageService stockMovingAverageService;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public Stock process(Stock stock) throws Exception {
    stockMovingAverageService.create(stock, LocalDate.parse(baseDateStr));
    return stock;
  }
}
