package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.support.AnalysisTestData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RsInsightServiceTest {

  private static final String STOCK_CODE = "005930";

  @Autowired RsInsightService rsInsightService;
  @Autowired AnalysisTestData analysisTestData;

  private long stockId;

  @BeforeEach
  void setUp() {
    stockId = analysisTestData.saveStock(STOCK_CODE, UNKNOWN);
  }

  @Test
  @DisplayName("RS 점수 반환")
  void returnsRsScore() {
    analysisTestData.saveRelativeStrength(stockId, 85);

    RsResponse result = rsInsightService.query(STOCK_CODE, LocalDate.now());

    assertSoftly(softly -> {
      softly.assertThat(result.rsValue()).isEqualByComparingTo(new BigDecimal("85"));
      softly.assertThat(result.percentileRank()).isEqualByComparingTo(new BigDecimal("85"));
    });
  }

  @Test
  @DisplayName("RS 데이터 없으면 예외 발생")
  void throwsExceptionWhenNoRsData() {
    assertThatThrownBy(() -> rsInsightService.query(STOCK_CODE, LocalDate.now()))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("여러 RS 기록 중 최신 기록 반환")
  void returnsLatestRsWhenMultipleRecordsExist() {
    analysisTestData.saveRelativeStrength(stockId, 50);
    analysisTestData.saveRelativeStrength(stockId, 75);

    RsResponse result = rsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.rsValue()).isEqualByComparingTo(new BigDecimal("75"));
  }

  @Test
  @DisplayName("다른 종목의 RS는 조회되지 않는다")
  void ignoresOtherStockRs() {
    long otherStockId = analysisTestData.saveStock("000660", UNKNOWN);
    analysisTestData.saveRelativeStrength(otherStockId, 99);
    analysisTestData.saveRelativeStrength(stockId, 40);

    RsResponse result = rsInsightService.query(STOCK_CODE, LocalDate.now());

    assertThat(result.rsValue()).isEqualByComparingTo(new BigDecimal("40"));
  }
}
