package com.momentum.application.insight;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_FAILED;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import com.momentum.support.AnalysisTestData;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RegimeInsightServiceTest {

  private static final LocalDate TODAY = LocalDate.now();

  @Autowired RegimeInsightService regimeInsightService;
  @Autowired AnalysisTestData analysisTestData;

  @Test
  @DisplayName("베이스 없으면 UNKNOWN 반환")
  void returnsUndeterminedWhenNoBase() {
    long stockId = analysisTestData.saveStock("005930", UNKNOWN);
    analysisTestData.saveCandle(stockId, TODAY, 10_000L, 100_000L);

    StockRegimeResponse result = regimeInsightService.query("005930", TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.regime()).isEqualTo(UNKNOWN);
      softly.assertThat(result.currentPrice()).isEqualTo(10_000L);
      softly.assertThat(result.supportLine()).isNull();
      softly.assertThat(result.resistanceLine()).isNull();
      softly.assertThat(result.changeRateFromReferenceLine()).isNull();
    });
  }

  @Test
  @DisplayName("레짐이 UNKNOWN이면 베이스가 있어도 UNKNOWN 반환")
  void returnsUndeterminedWhenRegimeUnknown() {
    long stockId = saveStockWithBase("000020", UNKNOWN, 10_000L, 8_000L, 9_000L);

    StockRegimeResponse result = regimeInsightService.query("000020", TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.regime()).isEqualTo(UNKNOWN);
      softly.assertThat(result.supportLine()).isNull();
      softly.assertThat(result.resistanceLine()).isNull();
    });
    assertSoftly(softly -> softly.assertThat(stockId).isPositive());
  }

  @Test
  @DisplayName("BREAKOUT_SUCCESS 레짐이면 저항선 대비 변동률 반환")
  void returnsChangeRateFromResistanceWhenBreakoutSuccess() {
    saveStockWithBase("000040", BREAKOUT_SUCCESS, 10_000L, 8_000L, 11_000L);

    StockRegimeResponse result = regimeInsightService.query("000040", TODAY);

    assertSoftly(softly -> {
      softly.assertThat(result.regime()).isEqualTo(BREAKOUT_SUCCESS);
      softly.assertThat(result.resistanceLine()).isEqualTo(10_000L);
      softly.assertThat(result.supportLine()).isEqualTo(8_000L);
      // (11000 - 10000) / 10000 * 100 = 10.0
      softly.assertThat(result.changeRateFromReferenceLine()).isEqualByComparingTo(new BigDecimal("10.0000"));
    });
  }

  @Test
  @DisplayName("DOWNSIDE_BREAK 레짐이면 지지선 대비 변동률 반환")
  void returnsChangeRateFromSupportWhenDownsideBreak() {
    saveStockWithBase("000050", DOWNSIDE_BREAK, 12_000L, 10_000L, 9_000L);

    StockRegimeResponse result = regimeInsightService.query("000050", TODAY);

    // (9000 - 10000) / 10000 * 100 = -10.0
    assertSoftly(softly -> {
      softly.assertThat(result.regime()).isEqualTo(DOWNSIDE_BREAK);
      softly.assertThat(result.changeRateFromReferenceLine()).isEqualByComparingTo(new BigDecimal("-10.0000"));
    });
  }

  @Test
  @DisplayName("BREAKOUT_FAILED 레짐이면 저항선 대비 음수 변동률 반환")
  void returnsNegativeChangeRateFromResistanceWhenBreakoutFailed() {
    saveStockWithBase("000070", BREAKOUT_FAILED, 10_000L, 8_000L, 9_500L);

    StockRegimeResponse result = regimeInsightService.query("000070", TODAY);

    // (9500 - 10000) / 10000 * 100 = -5.0
    assertSoftly(softly -> {
      softly.assertThat(result.regime()).isEqualTo(BREAKOUT_FAILED);
      softly.assertThat(result.changeRateFromReferenceLine()).isEqualByComparingTo(new BigDecimal("-5.0000"));
    });
  }

  private long saveStockWithBase(String code, StockRegime regime, long resistancePrice, long supportPrice,
      long currentPrice) {
    long stockId = analysisTestData.saveStock(code, regime);
    analysisTestData.saveCandle(stockId, TODAY, currentPrice, 100_000L);
    analysisTestData.saveBase(stockId, supportPrice, resistancePrice, TODAY.minusDays(20));
    return stockId;
  }
}
