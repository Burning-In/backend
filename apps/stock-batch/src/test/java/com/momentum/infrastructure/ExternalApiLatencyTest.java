package com.momentum.infrastructure;

import com.momentum.infrastructure.dto.StockCandleRequest;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * STEP 0-4 부품 실측 — 외부 API 1건 응답시간.
 * 실측 결과는 docs/성능 측정 기록.md 예측표에 기록한다.
 * 실행 방법: @Disabled 주석 처리 후 IDE에서 실행 (실 API 호출, 유효한 토큰 필요)
 */
@Disabled
@SpringBootTest
class ExternalApiLatencyTest {

  private static final String SAMSUNG = "005930";
  private static final LocalDate BASE_DATE = LocalDate.of(2026, 7, 9);
  private static final int SAMPLE_COUNT = 5;
  private static final long CALL_INTERVAL_MILLIS = 1_000; // LS/KIS 초당 호출 제한 회피

  @Autowired
  private LsStockChartClient lsStockChartClient;

  @Autowired
  private EpsProvider epsProvider;

  @Test
  void LS_캔들_API_1건_응답시간() throws InterruptedException {
    measure("LS 캔들(t8451)", () ->
        lsStockChartClient.getDailyCandles(StockCandleRequest.of(SAMSUNG, BASE_DATE)));
  }

  @Test
  void KIS_재무비율_API_1건_응답시간() throws InterruptedException {
    measure("KIS 재무비율(FHKST66430300)", () -> epsProvider.getQuarterlyEps(SAMSUNG));
  }

  private void measure(String name, Runnable call) throws InterruptedException {
    long warmup = elapsedMillis(call); // 최초 1회는 커넥션 수립 포함이라 따로 기록
    Thread.sleep(CALL_INTERVAL_MILLIS);

    long[] samples = new long[SAMPLE_COUNT];
    for (int i = 0; i < SAMPLE_COUNT; i++) {
      samples[i] = elapsedMillis(call);
      Thread.sleep(CALL_INTERVAL_MILLIS);
    }

    System.out.printf("[%s] 워밍업(커넥션 수립 포함): %dms%n", name, warmup);
    System.out.printf("[%s] 본측정: %s%n", name, Arrays.toString(samples));
    System.out.printf("[%s] 평균: %.1fms → 예측표에 기록%n",
        name, Arrays.stream(samples).average().orElse(0));
  }

  private long elapsedMillis(Runnable call) {
    long t0 = System.nanoTime();
    call.run();
    return (System.nanoTime() - t0) / 1_000_000;
  }
}
