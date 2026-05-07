package com.momentum.batch.job.ma;

import com.momentum.domain.ma.StockMovingAverageService;
import com.momentum.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockMaItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockMovingAverageService stockMovingAverageService;

  @Override
  public Stock process(Stock stock) throws Exception {
    stockMovingAverageService.create(stock);
    return stock;
  }
}
