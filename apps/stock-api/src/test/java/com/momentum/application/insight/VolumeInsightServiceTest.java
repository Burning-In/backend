package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.support.AnalysisTestData;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class VolumeInsightServiceTest {

  private static final LocalDate TODAY = LocalDate.now();
  private static final String STOCK_CODE = "005930";

  @Autowired VolumeInsightService volumeInsightService;
  @Autowired AnalysisTestData analysisTestData;

  private long stockId;

  @BeforeEach
  void setUp() {
    stockId = analysisTestData.saveStock(STOCK_CODE, UNKNOWN);
  }

  @Test
  @DisplayName("당일 거래량이 평균보다 높으면 ratio 1 초과")
  void ratioExceedsOneWhenCurrentVolumeAboveAverage() {
    saveHistoricalCandles(30, 1_000L);
    analysisTestData.saveCandle(stockId, TODAY, 10_000L, 10_000L);

    VolumeResponse result = volumeInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.currentVolume()).isEqualTo(10_000L);
      softly.assertThat(result.volumeToBaselineRatio()).isGreaterThan(BigDecimal.ONE);
    });
  }

  @Test
  @DisplayName("당일 거래량이 평균보다 낮으면 ratio 1 미만")
  void ratioBelowOneWhenCurrentVolumeUnderAverage() {
    saveHistoricalCandles(30, 10_000L);
    analysisTestData.saveCandle(stockId, TODAY, 10_000L, 1_000L);

    VolumeResponse result = volumeInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.currentVolume()).isEqualTo(1_000L);
      softly.assertThat(result.volumeToBaselineRatio()).isLessThan(BigDecimal.ONE);
    });
  }

  @Test
  @DisplayName("모든 날 거래량이 같으면 ratio 1")
  void ratioIsOneWhenVolumeEqualsAverage() {
    saveHistoricalCandles(10, 5_000L);
    analysisTestData.saveCandle(stockId, TODAY, 10_000L, 5_000L);

    VolumeResponse result = volumeInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.volumeToBaselineRatio()).isEqualByComparingTo(BigDecimal.ONE);
  }

  private void saveHistoricalCandles(int days, long volume) {
    for (int daysAgo = 1; daysAgo <= days; daysAgo++) {
      analysisTestData.saveCandle(stockId, TODAY.minusDays(daysAgo), 10_000L, volume);
    }
  }
}
