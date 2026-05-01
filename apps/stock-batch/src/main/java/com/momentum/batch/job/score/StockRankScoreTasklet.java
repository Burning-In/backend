package com.momentum.batch.job.score;

import com.momentum.domain.score.StockRankScoreService;
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
public class StockRankScoreTasklet implements Tasklet {

  private final StockRankScoreService stockRankScoreService;

  @Value("#{jobParameters['baseDate']}")
  private String baseDateStr;

  @Override
  public RepeatStatus execute(StepContribution contribution,
      ChunkContext chunkContext) throws Exception {
    LocalDate baseDate = LocalDate.parse(baseDateStr);
    stockRankScoreService.calculateDailyRankScores(baseDate);
    return RepeatStatus.FINISHED;
  }
}

// 애도 저장을 좀 다르게 해야되는데
