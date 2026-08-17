package com.momentum.domain.movingaverage;

import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_150;
import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_200;
import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_50;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockMovingAverageServiceTest {

  private static final LocalDate TODAY = LocalDate.now();

  @Autowired
  private StockMovingAverageService stockMovingAverageService;
  @Autowired
  private StockMovingAverageRepository stockMovingAverageRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.UPTREND));
  }

  @ParameterizedTest(name = "캔들 {0}개 → 이동평균 {1}개")
  @CsvSource({"49, 0", "50, 1", "149, 1", "150, 2", "199, 2", "200, 3"})
  @DisplayName("거래일 수가 기간을 채운 이동평균만 만들어진다")
  void create_makesOnlyPeriodsCoveredByCandles(int candleCount, int expectedCount) {
    // given
    saveCandles(1, candleCount, 1_000L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, TODAY);

    // then
    assertThat(result).hasSize(expectedCount);
  }

  @Test
  @DisplayName("거래일이 50개뿐이면 MA_50만 만들어진다")
  void create_withFiftyCandles_makesOnlyMa50() {
    // given
    saveCandles(1, 50, 1_000L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, TODAY);

    // then
    assertThat(result).extracting(StockMovingAverage::getStockMovingAveragePeriod)
        .containsExactly(MA_50);
  }

  @Test
  @DisplayName("각 이동평균은 자기 기간만큼의 최근 종가만 평균낸다")
  void create_averagesOnlyRecentClosesOfEachPeriod() {
    // given
    saveCandles(1, 50, 3_000L);
    saveCandles(51, 150, 1_000L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, TODAY);

    // then
    assertSoftly(softly -> {
      softly.assertThat(maOf(result, MA_50)).isEqualTo(3_000L);
      softly.assertThat(maOf(result, MA_150)).isEqualTo((50 * 3_000L + 100 * 1_000L) / 150);
      softly.assertThat(maOf(result, MA_200)).isEqualTo((50 * 3_000L + 150 * 1_000L) / 200);
    });
  }

  @Test
  @DisplayName("오래된 거래일의 종가는 짧은 기간의 이동평균에 섞이지 않는다")
  void create_doesNotMixOlderClosesIntoShorterPeriod() {
    // given
    saveCandles(1, 50, 1_000L);
    saveCandles(51, 150, 9_999L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, TODAY);

    // then
    assertThat(maOf(result, MA_50)).isEqualTo(1_000L);
  }

  @Test
  @DisplayName("평균이 나누어떨어지지 않으면 소수점을 버린다")
  void create_truncatesFractionalAverage() {
    // given
    saveCandles(1, 49, 1_000L);
    saveCandles(50, 1, 1_075L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, TODAY);

    // then
    assertThat(maOf(result, MA_50)).isEqualTo(1_001L);
  }

  @Test
  @DisplayName("만들어진 이동평균은 저장되어 다시 조회된다")
  void create_persistsMovingAverages() {
    // given
    saveCandles(1, 200, 1_000L);

    // when
    stockMovingAverageService.create(stock, TODAY);

    // then
    assertThat(stockMovingAverageRepository.findLatestByStock(stock, TODAY)).hasSize(3);
  }

  @Test
  @DisplayName("이동평균은 계산 기준일과 함께 기록된다")
  void create_recordsBaseDate() {
    // given
    LocalDate baseDate = TODAY.minusDays(10);
    saveCandles(11, 50, 1_000L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, baseDate);

    // then
    assertThat(result).extracting(StockMovingAverage::getBaseDate).containsOnly(baseDate);
  }

  @Test
  @DisplayName("기준일 이후의 거래일은 이동평균에 들어가지 않는다")
  void create_ignoresCandlesAfterBaseDate() {
    // given
    saveCandles(1, 10, 9_999L);
    saveCandles(11, 50, 1_000L);

    // when
    List<StockMovingAverage> result = stockMovingAverageService.create(stock, TODAY.minusDays(11));

    // then
    assertThat(maOf(result, MA_50)).isEqualTo(1_000L);
  }

  private void saveCandles(int startDaysAgo, int count, long closePrice) {
    List<StockDailyCandle> candles = new ArrayList<>();
    for (int offset = 0; offset < count; offset++) {
      String tradeDate = TODAY.minusDays(startDaysAgo + offset).format(DateTimeFormatter.BASIC_ISO_DATE);
      candles.add(StockDailyCandle.create(stock, tradeDate, closePrice, closePrice, closePrice, closePrice, 1_000L));
    }
    stockCandleRepository.saveAll(candles);
  }

  private long maOf(List<StockMovingAverage> movingAverages, StockMovingAveragePeriod period) {
    return movingAverages.stream()
        .filter(movingAverage -> movingAverage.getStockMovingAveragePeriod() == period)
        .findFirst()
        .orElseThrow()
        .getMa();
  }
}
