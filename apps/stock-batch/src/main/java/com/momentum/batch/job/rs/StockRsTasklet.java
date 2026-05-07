package com.momentum.batch.job.rs;

import com.momentum.application.StockRsFacade;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class StockRsTasklet implements Tasklet {

  private final StockRsFacade stockRsFacade;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    stockRsFacade.calculate(LocalDate.parse(baseDateStr));
    return RepeatStatus.FINISHED;
  }
}
