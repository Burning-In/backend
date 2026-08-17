package com.momentum.application;

import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_50;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse.BaseItem;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.DailyCandleResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.MovingAverageResponse;
import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.support.AnalysisTestData;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockChartServiceTest {

  @Autowired
  private StockChartService stockChartService;
  @Autowired
  private AnalysisTestData analysisTestData;

  @Test
  @DisplayName("일봉은 요청 날짜 범위만 오름차순으로 반환한다")
  void getDailyCandlesReturnsRange() {
    long stockId = saveStock("000520");
    analysisTestData.saveCandle(stockId, LocalDate.of(2026, 5, 1), 100L, 1_000L);
    analysisTestData.saveCandle(stockId, LocalDate.of(2026, 5, 2), 200L, 1_000L);
    analysisTestData.saveCandle(stockId, LocalDate.of(2026, 5, 3), 300L, 1_000L);

    DailyCandleResponse response =
        stockChartService.getDailyCandles("000520", LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3));

    assertSoftly(softly -> {
      softly.assertThat(response.candles()).hasSize(2);
      softly.assertThat(response.candles().get(0).closePrice()).isEqualTo(200L);
      softly.assertThat(response.candles().get(1).closePrice()).isEqualTo(300L);
    });
  }

  @Test
  @DisplayName("이평선은 최근 N거래일 종가 평균을 롤링으로 계산한다")
  void getMovingAveragesComputesRollingSma() {
    long stockId = saveStock("000080");
    LocalDate start = LocalDate.of(2026, 1, 1);
    saveFlatCandles(stockId, start, 50, 1_000L);              // index 0~49 종가 1000
    analysisTestData.saveCandle(stockId, start.plusDays(50), 2_000L, 1_000L); // index 50 종가 2000

    MovingAverageResponse response = stockChartService.getMovingAverages(
        "000080", MA_50, start.plusDays(49), start.plusDays(50));

    assertSoftly(softly -> {
      softly.assertThat(response.period()).isEqualTo(MA_50);
      softly.assertThat(response.dataPoints()).hasSize(2);
      softly.assertThat(response.dataPoints().get(0).price()).isEqualTo(1_000L);   // 50개 평균
      softly.assertThat(response.dataPoints().get(1).price()).isEqualTo(1_020L);   // (49*1000+2000)/50
    });
  }

  @Test
  @DisplayName("N거래일치 데이터가 모이지 않으면 이평선 값이 없다")
  void getMovingAveragesSkipsDaysWithoutFullWindow() {
    long stockId = saveStock("000100");
    LocalDate start = LocalDate.of(2026, 1, 1);
    saveFlatCandles(stockId, start, 30, 1_000L);              // 30개(<50)

    MovingAverageResponse response =
        stockChartService.getMovingAverages("000100", MA_50, start, start.plusDays(29));

    assertThat(response.dataPoints()).isEmpty();
  }

  @Test
  @DisplayName("베이스는 지지/저항 가격을 반환하고 최신 베이스의 endDate는 null이다")
  void getBasesMapsCurrentBaseWithNullEndDate() {
    long stockId = saveStock("000660");
    analysisTestData.saveBase(stockId, 8_000L, 10_000L, LocalDate.now());

    BaseListResponse response = stockChartService.getBases(
        "000660", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

    assertSoftly(softly -> {
      softly.assertThat(response.bases()).hasSize(1);
      BaseItem item = response.bases().get(0);
      softly.assertThat(item.endDate()).isNull();
      softly.assertThat(item.supportPrice()).isEqualTo(8_000L);
      softly.assertThat(item.resistancePrice()).isEqualTo(10_000L);
      softly.assertThat(item.startDate()).isEqualTo(LocalDate.now());
    });
  }

  @Test
  @DisplayName("베이스가 여러 개면 다음 베이스 시작일이 이전 베이스의 종료일이 된다")
  void getBasesUsesNextBaseStartAsEndDate() {
    long stockId = saveStock("000670");
    analysisTestData.saveBase(stockId, 8_000L, 10_000L, LocalDate.now().minusDays(10));
    analysisTestData.saveBase(stockId, 9_000L, 11_000L, LocalDate.now());

    BaseListResponse response = stockChartService.getBases(
        "000670", LocalDate.now().minusDays(30), LocalDate.now().plusDays(1));

    assertSoftly(softly -> {
      softly.assertThat(response.bases()).hasSize(2);
      softly.assertThat(response.bases().get(0).endDate()).isEqualTo(LocalDate.now());
      softly.assertThat(response.bases().get(1).endDate()).isNull();
    });
  }

  private long saveStock(String code) {
    return analysisTestData.saveStock(code, BREAKOUT_READY);
  }

  private void saveFlatCandles(long stockId, LocalDate start, int count, long closePrice) {
    for (int i = 0; i < count; i++) {
      analysisTestData.saveCandle(stockId, start.plusDays(i), closePrice, 1_000L);
    }
  }
}
