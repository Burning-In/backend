package com.momentum.batch.job;

import com.momentum.batch.job.pricepoint.StockPricePointItemProcessor;
import com.momentum.batch.job.pricepoint.StockPricePointItemWriter;
import com.momentum.batch.job.regime.StockRegimeItemProcessor;
import com.momentum.batch.job.regime.StockRegimeItemWriter;
import com.momentum.batch.job.score.StockRankScoreTasklet;
import com.momentum.batch.job.stockcandle.StockCandleItemProcessor;
import com.momentum.batch.job.stockcandle.StockCandleItemWriter;
import com.momentum.batch.job.vcp.StockVcpItemProcessor;
import com.momentum.batch.job.vcp.StockVcpItemWriter;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.StockDailyCandle;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DailyStockJobConfig {

  private static final int CHUNK_SIZE = 50;

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  private final StockCandleItemProcessor stockCandleItemProcessor;
  private final StockCandleItemWriter stockCandleItemWriter;

  private final StockPricePointItemProcessor stockPricePointItemProcessor;
  private final StockPricePointItemWriter stockPricePointItemWriter;

  private final StockVcpItemProcessor stockVcpItemProcessor;
  private final StockVcpItemWriter stockVcpItemWriter;

  private final StockRegimeItemProcessor stockRegimeItemProcessor;
  private final StockRegimeItemWriter stockRegimeItemWriter;

  private final StockRankScoreTasklet stockRankScoreTasklet;
  private final EntityManagerFactory emf;

  @Bean
  @StepScope
  public JpaPagingItemReader<Stock> stockItemReader() {
    return new JpaPagingItemReaderBuilder<Stock>()
        .name("stockItemReader")
        .entityManagerFactory(emf)
        .queryString("SELECT s FROM Stock s ORDER BY s.id")
        .pageSize(CHUNK_SIZE)
        .build();
  }

  @Bean
  public Job dailyStockJob() {
    return new JobBuilder("dailyStockJob", jobRepository)
        .start(stockCandleStep())
        .next(stockPricePointStep())
        .next(stockVcpStep())
        .next(stockRegimeStep())
        .next(stockRankScoreStep())
        .build();
  }

  @Bean
  public Step stockCandleStep() {
    return new StepBuilder("stockCandleStep", jobRepository)
        .<Stock, StockDailyCandle>chunk(CHUNK_SIZE, transactionManager)
        .reader(stockItemReader())
        .processor(stockCandleItemProcessor)
        .writer(stockCandleItemWriter)
        .build();
  }

  @Bean
  public Step stockPricePointStep() {
    return new StepBuilder("stockPricePointStep", jobRepository)
        .<Stock, Stock>chunk(CHUNK_SIZE, transactionManager)
        .reader(stockItemReader())
        .processor(stockPricePointItemProcessor)
        .writer(stockPricePointItemWriter)
        .build();
  }

  @Bean
  public Step stockVcpStep() {
    return new StepBuilder("stockVcpStep", jobRepository)
        .<Stock, Stock>chunk(CHUNK_SIZE, transactionManager)
        .reader(stockItemReader())
        .processor(stockVcpItemProcessor)
        .writer(stockVcpItemWriter)
        .build();
  }

  @Bean
  public Step stockRegimeStep() {
    return new StepBuilder("stockRegimeStep", jobRepository)
        .<Stock, Stock>chunk(CHUNK_SIZE, transactionManager)
        .reader(stockItemReader())
        .processor(stockRegimeItemProcessor)
        .writer(stockRegimeItemWriter)
        .build();
  }

  // Step5는 종목 단위가 아니라 전체 한번에 계산
  @Bean
  public Step stockRankScoreStep() {
    return new StepBuilder("stockRankScoreStep", jobRepository)
        .tasklet(stockRankScoreTasklet, transactionManager)
        .build();
  }
}
