package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.support.AnalysisTestData;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class EpsInsightServiceTest {

  private static final String STOCK_CODE = "005930";
  private static final int QUARTER_MONTHS = 3;

  @Autowired EpsInsightService epsInsightService;
  @Autowired AnalysisTestData analysisTestData;

  private long stockId;

  @BeforeEach
  void setUp() {
    stockId = analysisTestData.saveStock(STOCK_CODE, UNKNOWN);
  }

  @Test
  @DisplayName("EPS 데이터 없으면 빈 리스트 반환")
  void returnsEmptyListWhenNoEpsData() {
    EpsResponse result = epsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.quarterlyEps()).isEmpty();
    assertThat(result.changeRateYoY()).isNull();
  }

  @Test
  @DisplayName("6분기 있어도 최근 5분기만 반환")
  void returnsOnlyFiveQuartersWhenMoreExist() {
    saveQuarterlyEps(6);

    EpsResponse result = epsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.quarterlyEps()).hasSize(5);
  }

  @Test
  @DisplayName("3분기만 있으면 3분기 반환")
  void returnsThreeQuartersWhenOnlyThreeExist() {
    saveQuarterlyEps(3);

    EpsResponse result = epsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.quarterlyEps()).hasSize(3);
  }

  @Test
  @DisplayName("최신 분기의 YoY 변동률 반환")
  void returnsYoyChangeRateFromLatestQuarter() {
    YearMonth latestQuarter = YearMonth.now();
    analysisTestData.saveEps(stockId, latestQuarter, 1500.0, 0.25);
    analysisTestData.saveEps(stockId, latestQuarter.minusMonths(QUARTER_MONTHS), 1200.0, null);

    EpsResponse result = epsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.changeRateYoY()).isEqualByComparingTo("0.2500");
  }

  @Test
  @DisplayName("최근 분기가 먼저 오도록 분기 내림차순으로 반환한다")
  void returnsQuartersInDescendingOrder() {
    analysisTestData.saveEps(stockId, YearMonth.of(2024, 3), 1000.0, null);
    analysisTestData.saveEps(stockId, YearMonth.of(2024, 12), 1300.0, null);
    analysisTestData.saveEps(stockId, YearMonth.of(2024, 6), 1100.0, null);

    EpsResponse result = epsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.quarterlyEps()).extracting("quarter")
        .containsExactly("2024-12", "2024-06", "2024-03");
  }

  @Test
  @DisplayName("분기명이 YearMonth 형식으로 반환")
  void returnsQuarterNameInYearMonthFormat() {
    analysisTestData.saveEps(stockId, YearMonth.of(2024, 3), 1000.0, null);

    EpsResponse result = epsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.quarterlyEps().get(0).quarter()).isEqualTo("2024-03");
  }

  private void saveQuarterlyEps(int count) {
    YearMonth base = YearMonth.now();
    for (int i = 0; i < count; i++) {
      analysisTestData.saveEps(stockId, base.minusMonths((long) i * QUARTER_MONTHS), 1000.0 + i * 100, null);
    }
  }
}
