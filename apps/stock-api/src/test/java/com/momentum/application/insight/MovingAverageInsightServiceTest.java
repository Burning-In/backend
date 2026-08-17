package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_150;
import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_200;
import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_50;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.support.AnalysisTestData;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MovingAverageInsightServiceTest {

  private static final LocalDate TODAY = LocalDate.now();
  private static final String STOCK_CODE = "005930";
  private static final long CURRENT_PRICE = 10_000L;

  @Autowired MovingAverageInsightService movingAverageInsightService;
  @Autowired AnalysisTestData analysisTestData;

  private long stockId;

  @BeforeEach
  void setUp() {
    stockId = analysisTestData.saveStock(STOCK_CODE, UNKNOWN);
    analysisTestData.saveCandle(stockId, TODAY, CURRENT_PRICE, 100_000L);
  }

  @Test
  @DisplayName("현재가 > MA50 > MA150 > MA200 이면 정배열 true")
  void isAlignedTrueWhenPricesInOrder() {
    saveMas(9_000L, 8_000L, 7_000L);

    MovingAverageResponse result = movingAverageInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.isAboveMa50()).isTrue();
      softly.assertThat(result.isMa50AboveMa150()).isTrue();
      softly.assertThat(result.isMa150AboveMa200()).isTrue();
      softly.assertThat(result.currentPrice()).isEqualTo(CURRENT_PRICE);
      softly.assertThat(result.ma50()).isEqualTo(9_000L);
      softly.assertThat(result.ma150()).isEqualTo(8_000L);
      softly.assertThat(result.ma200()).isEqualTo(7_000L);
    });
  }

  @Test
  @DisplayName("현재가가 MA50보다 낮으면 정배열 false")
  void isAlignedFalseWhenPriceBelowMa50() {
    saveMas(11_000L, 8_000L, 7_000L);

    MovingAverageResponse result = movingAverageInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.isAboveMa50()).isFalse();
  }

  @Test
  @DisplayName("MA50이 MA150보다 낮으면 정배열 false")
  void isAlignedFalseWhenMa50BelowMa150() {
    saveMas(9_000L, 9_500L, 7_000L);

    MovingAverageResponse result = movingAverageInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.isMa50AboveMa150()).isFalse();
  }

  @Test
  @DisplayName("MA150이 MA200보다 낮으면 정배열 false")
  void isAlignedFalseWhenMa150BelowMa200() {
    saveMas(9_000L, 8_000L, 8_500L);

    MovingAverageResponse result = movingAverageInsightService.query(STOCK_CODE, TODAY);

    assertThat(result.isMa150AboveMa200()).isFalse();
  }

  @Test
  @DisplayName("MA 데이터 없으면 정배열 false, MA 값 null")
  void isAlignedFalseWhenNoMaData() {
    MovingAverageResponse result = movingAverageInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.isAboveMa50()).isFalse();
      softly.assertThat(result.isMa50AboveMa150()).isFalse();
      softly.assertThat(result.isMa150AboveMa200()).isFalse();
      softly.assertThat(result.ma50()).isNull();
      softly.assertThat(result.ma150()).isNull();
      softly.assertThat(result.ma200()).isNull();
    });
  }

  private void saveMas(long ma50, long ma150, long ma200) {
    analysisTestData.saveMovingAverage(stockId, MA_50, ma50, TODAY);
    analysisTestData.saveMovingAverage(stockId, MA_150, ma150, TODAY);
    analysisTestData.saveMovingAverage(stockId, MA_200, ma200, TODAY);
  }
}
