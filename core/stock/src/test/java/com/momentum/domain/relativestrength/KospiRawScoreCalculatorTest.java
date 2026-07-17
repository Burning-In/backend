package com.momentum.domain.relativestrength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class KospiRawScoreCalculatorTest {

  private static final double RECENT_QUARTER_WEIGHT = 0.4;
  private static final double HALF_YEAR_WEIGHT = 0.2;
  private static final double THREE_QUARTERS_WEIGHT = 0.2;
  private static final double FULL_YEAR_WEIGHT = 0.2;

  @Autowired
  private KospiRawScoreCalculator calculator;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private KospiRepository kospiRepository;

  @Autowired
  private RsTestSupport rsTestSupport;

  @Test
  @DisplayName("주식 가격과 KOSPI 지수가 모두 일정할 때 RSRawScore는 100이다")
  void calculateScores_whenAllPricesFlat_returnsHundred() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(Stock.of("테스트주식", "000040", StockRegime.UNKNOWN, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stock), today,
        RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT);

    // then
    assertThat(scores).hasSize(1);
    assertThat(scores.get(0).rsRawScore()).isCloseTo(100.0, within(0.001));
  }

  @Test
  @DisplayName("주식이 지수보다 강하면 RSRawScore는 100보다 크다")
  void calculateScores_whenStockStrongerThanIndex_returnsAboveHundred() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(Stock.of("강한주식", "000050", StockRegime.UNKNOWN, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stock, today, 12_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stock), today,
        RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT);

    // then
    assertThat(scores).hasSize(1);
    assertThat(scores.get(0).rsRawScore()).isGreaterThan(100.0);
  }

  @Test
  @DisplayName("주식이 지수보다 약하면 RSRawScore는 100보다 작다")
  void calculateScores_whenStockWeakerThanIndex_returnsBelowHundred() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(Stock.of("약한주식", "000070", StockRegime.UNKNOWN, StockTrend.UPTREND));

    // Kospi 상승, 주식 횡보
    kospiRepository.save(new Kospi(3000L, today));
    rsTestSupport.setupKospi(today.minusMonths(3), 2500L);
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stock), today,
        RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT);

    // then
    assertThat(scores).hasSize(1);
    assertThat(scores.get(0).rsRawScore()).isLessThan(100.0);
  }

  @Test
  @DisplayName("여러 주식의 RSRawScore를 계산하면 각 주식에 대한 점수가 반환된다")
  void calculateScores_withMultipleStocks_returnsScoreForEach() {
    // given
    LocalDate today = LocalDate.now();
    Stock stockA = stockRepository.save(Stock.of("주식A", "000080", StockRegime.UNKNOWN, StockTrend.UPTREND));
    Stock stockB = stockRepository.save(Stock.of("주식B", "000100", StockRegime.UNKNOWN, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stockA, today, 12_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(stockB, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stockA, stockB), today,
        RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT);

    // then
    assertThat(scores).hasSize(2);
    RSRawScore scoreA = scores.stream().filter(s -> s.stockCode().equals(stockA)).findFirst().orElseThrow();
    RSRawScore scoreB = scores.stream().filter(s -> s.stockCode().equals(stockB)).findFirst().orElseThrow();
    assertThat(scoreA.rsRawScore()).isGreaterThan(scoreB.rsRawScore());
  }

  @Test
  @DisplayName("캔들 데이터가 없으면 예외가 발생한다")
  void calculateScores_whenCandleMissing_throwsException() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(Stock.of("데이터없음", "000270", StockRegime.UNKNOWN, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);

    // when & then
    assertThatThrownBy(() -> calculator.calculateScores(List.of(stock), today,
        RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("KOSPI 데이터가 없으면 예외가 발생한다")
  void calculateScores_whenKospiMissing_throwsException() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(Stock.of("코스피없음", "000240", StockRegime.UNKNOWN, StockTrend.UPTREND));

    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when & then
    assertThatThrownBy(() -> calculator.calculateScores(List.of(stock), today,
        RECENT_QUARTER_WEIGHT, HALF_YEAR_WEIGHT, THREE_QUARTERS_WEIGHT, FULL_YEAR_WEIGHT))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
