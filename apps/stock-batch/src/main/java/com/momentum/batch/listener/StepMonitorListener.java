package com.momentum.batch.listener;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class StepMonitorListener implements StepExecutionListener {

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        log.info("Step '{}' 시작", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        var endTime = stepExecution.getEndTime() == null
                ? java.time.LocalDateTime.now() : stepExecution.getEndTime();
        long seconds = java.time.Duration.between(stepExecution.getStartTime(), endTime).getSeconds();
        log.info("Step '{}' 종료 — {}초, read={}, write={}",
                stepExecution.getStepName(), seconds,
                stepExecution.getReadCount(), stepExecution.getWriteCount());

        if (!stepExecution.getFailureExceptions().isEmpty()) {
            var jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
            var exceptions = stepExecution.getFailureExceptions().stream()
                    .map(Throwable::getMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
            log.info(
                """
                   [에러 발생]
                   jobName: {}
                   exceptions:
                   {}
               """.trim(), jobName, exceptions
            );
            // error 발생 시 slack 등 다른 채널로 모니터 전송
            return ExitStatus.FAILED;
        }
        return ExitStatus.COMPLETED;
    }
}
