package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
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
class FrogInPanInsightServiceTest {

  private static final LocalDate TODAY = LocalDate.now();
  private static final String STOCK_CODE = "005930";
  // FIP = (상승일 - 하락일) / 252. 251일 전부 상승이면 251/252
  private static final double ALL_UP_FIP = 0.996032;

  @Autowired FrogInPanInsightService frogInPanInsightService;
  @Autowired AnalysisTestData analysisTestData;

  private long stockId;

  @BeforeEach
  void setUp() {
    stockId = analysisTestData.saveStock(STOCK_CODE, UNKNOWN);
  }

  @Test
  @DisplayName("저장된 FIP 점수 반환")
  void returnsFipScoreFromRankScore() {
    analysisTestData.saveRankScore(stockId, TODAY, 0.2, ALL_UP_FIP, 251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.fipScore()).isEqualByComparingTo(new BigDecimal("0.996032"));
  }

  @Test
  @DisplayName("252일 모두 상승이면 upDays = 251, downDays = 0")
  void countsUpDaysCorrectlyWhenAllDaysUp() {
    analysisTestData.saveRankScore(stockId, TODAY, 0.2, ALL_UP_FIP, 251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.yearlyUpDays()).isEqualTo(251);
      softly.assertThat(result.yearlyDownDays()).isEqualTo(0);
    });
  }

  @Test
  @DisplayName("252일 모두 하락이면 upDays = 0, downDays = 251")
  void countsDownDaysCorrectlyWhenAllDaysDown() {
    analysisTestData.saveRankScore(stockId, TODAY, -0.2, ALL_UP_FIP, 0, 251);

    FrogInPanResponse result = frogInPanInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.yearlyUpDays()).isEqualTo(0);
      softly.assertThat(result.yearlyDownDays()).isEqualTo(251);
    });
  }

  @Test
  @DisplayName("상승일과 하락일이 절반씩이면 upDays = downDays")
  void upDaysEqualsDownDaysWhenEqualUpAndDown() {
    analysisTestData.saveRankScore(stockId, TODAY, 0.0, 0.0, 125, 125);

    FrogInPanResponse result = frogInPanInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(result.yearlyDownDays());
  }

  @Test
  @DisplayName("여러 기준일이 쌓여 있으면 가장 최근 기준일 점수를 반환한다")
  void returnsLatestBaseDateScore() {
    analysisTestData.saveRankScore(stockId, TODAY.minusDays(1), 0.1, 0.1, 10, 20);
    analysisTestData.saveRankScore(stockId, TODAY, 0.2, ALL_UP_FIP, 251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(251);
  }
}
