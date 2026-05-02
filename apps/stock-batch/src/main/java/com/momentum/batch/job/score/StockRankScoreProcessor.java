package com.momentum.batch.job.score;

import com.momentum.domain.score.StockRankScoreService;
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
public class StockRankScoreProcessor implements ItemProcessor<Stock, Stock> {

  private final StockRankScoreService stockRankScoreService;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public Stock process(Stock stock) throws Exception {
    LocalDate baseDate = LocalDate.parse(baseDateStr);
    stockRankScoreService.calculateDailyRankScores(stock, baseDate);
    return stock;
  }
}
