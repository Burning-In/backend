package com.momentum.batch.job.eps;

import com.momentum.application.StockEpsFacade;
import com.momentum.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockEpsItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockEpsFacade stockEpsFacade;

  @Override
  public Stock process(Stock stock) throws Exception {
    stockEpsFacade.record(stock);
    return stock;
  }
}
