package com.momentum.batch.job;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyStockJobLauncher {

  private final JobLauncher jobLauncher;
  private final Job dailyStockJob;

  // 매일 오전 1시 실행
  @Scheduled(cron = "0 0 21 * * *")
  public void run() throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("baseDate", LocalDate.now().toString())
        .toJobParameters();

    jobLauncher.run(dailyStockJob, jobParameters);
  }
}
