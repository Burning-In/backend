package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
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
class MomentumScoreInsightServiceTest {

  private static final LocalDate TODAY = LocalDate.now();
  private static final String STOCK_CODE = "005930";
  private static final int TRADING_DAYS_PER_YEAR = 252;

  @Autowired MomentumInsightService momentumInsightService;
  @Autowired AnalysisTestData analysisTestData;

  private long stockId;

  @BeforeEach
  void setUp() {
    stockId = analysisTestData.saveStock(STOCK_CODE, UNKNOWN);
  }

  @Test
  @DisplayName("StockRankScore의 momentum을 퍼센트로 변환해 반환")
  void returnsYearlyChangeRateFromRankScore() {
    saveYearOfCandles(10_000L, 12_000L);
    analysisTestData.saveRankScore(stockId, TODAY, 0.2, 0.1, 130, 120);

    MomentumResponse result = momentumInsightService.query(STOCK_CODE, TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.currentPrice()).isEqualTo(12_000L);
      softly.assertThat(result.yearAgoPrice()).isEqualTo(10_000L);
      softly.assertThat(result.yearlyPriceChangeRate()).isEqualByComparingTo(new BigDecimal("20.0000"));
    });
  }

  @Test
  @DisplayName("모멘텀이 하위권이면 낮은 퍼센타일 반환")
  void returnsLowPercentileWhenMomentumIsLow() {
    saveYearOfCandles(10_000L, 10_500L);
    savePeerRankScore("000040", 0.10);
    savePeerRankScore("000050", 0.20);
    savePeerRankScore("000070", 0.30);
    analysisTestData.saveRankScore(stockId, TODAY, 0.05, 0.1, 130, 120);

    MomentumResponse result = momentumInsightService.query(STOCK_CODE, TODAY);

    // 0.05보다 낮은 종목 = 0개 → 0/4 * 100 = 0.0%
    assertThat(result.percentileRank()).isEqualByComparingTo(new BigDecimal("0.0"));
  }

  @Test
  @DisplayName("모멘텀이 상위권이면 높은 퍼센타일 반환")
  void returnsHighPercentileWhenMomentumIsHigh() {
    saveYearOfCandles(10_000L, 13_000L);
    savePeerRankScore("000040", 0.05);
    savePeerRankScore("000050", 0.10);
    savePeerRankScore("000070", 0.20);
    analysisTestData.saveRankScore(stockId, TODAY, 0.30, 0.1, 130, 120);

    MomentumResponse result = momentumInsightService.query(STOCK_CODE, TODAY);

    // 0.30보다 낮은 종목 = 3개 → 3/4 * 100 = 75.0%
    assertThat(result.percentileRank()).isEqualByComparingTo(new BigDecimal("75.0"));
  }

  private void savePeerRankScore(String code, double momentum) {
    long peerStockId = analysisTestData.saveStock(code, UNKNOWN);
    analysisTestData.saveRankScore(peerStockId, TODAY, momentum, 0.1, 130, 120);
  }

  private void saveYearOfCandles(long yearAgoClose, long todayClose) {
    analysisTestData.saveCandle(stockId, TODAY.minusDays(TRADING_DAYS_PER_YEAR), yearAgoClose, 100_000L);
    for (int daysAgo = 1; daysAgo < TRADING_DAYS_PER_YEAR; daysAgo++) {
      analysisTestData.saveCandle(stockId, TODAY.minusDays(daysAgo), 11_000L, 100_000L);
    }
    analysisTestData.saveCandle(stockId, TODAY, todayClose, 100_000L);
  }
}
