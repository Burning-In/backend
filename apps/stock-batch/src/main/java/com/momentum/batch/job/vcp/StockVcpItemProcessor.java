package com.momentum.batch.job.vcp;

import com.momentum.domain.base.service.StockBaseVolatilityContractionPatternService;
import com.momentum.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockVcpItemProcessor implements ItemProcessor<Stock, Stock> {

  private final StockBaseVolatilityContractionPatternService vcpService;

  @Override
  public Stock process(Stock stock) throws Exception {
    vcpService.calculateVolatilityContractionPattern(stock.getId());
    return stock;
  }
}
