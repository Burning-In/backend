package com.momentum.domain.rs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.momentum.domain.rs.KOSPIRawScoreCalculator.RSRawScore;
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
class KOSPIRawScoreCalculatorTest {

  @Autowired
  private KOSPIRawScoreCalculator calculator;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private KOSPIRepository kospiRepository;

  @Autowired
  private RsTestSupport rsTestSupport;

  @Test
  @DisplayName("주식 가격과 KOSPI 지수가 모두 일정할 때 RSRawScore는 100이다")
  void calculateScores_whenAllPricesFlat_returnsHundred() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(new Stock("테스트주식", "000001", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stock), today);

    // then
    assertThat(scores).hasSize(1);
    assertThat(scores.get(0).rsRawScore()).isCloseTo(100.0, within(0.001));
  }

  @Test
  @DisplayName("주식이 지수보다 강하면 RSRawScore는 100보다 크다")
  void calculateScores_whenStockStrongerThanIndex_returnsAboveHundred() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(new Stock("강한주식", "000002", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stock, today, 12_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stock), today);

    // then
    assertThat(scores).hasSize(1);
    assertThat(scores.get(0).rsRawScore()).isGreaterThan(100.0);
  }

  @Test
  @DisplayName("주식이 지수보다 약하면 RSRawScore는 100보다 작다")
  void calculateScores_whenStockWeakerThanIndex_returnsBelowHundred() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(new Stock("약한주식", "000003", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    // KOSPI 상승, 주식 횡보
    kospiRepository.save(new KOSPI(3000L, today));
    rsTestSupport.setupKospi(today.minusMonths(3), 2500L);
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stock), today);

    // then
    assertThat(scores).hasSize(1);
    assertThat(scores.get(0).rsRawScore()).isLessThan(100.0);
  }

  @Test
  @DisplayName("여러 주식의 RSRawScore를 계산하면 각 주식에 대한 점수가 반환된다")
  void calculateScores_withMultipleStocks_returnsScoreForEach() {
    // given
    LocalDate today = LocalDate.now();
    Stock stockA = stockRepository.save(new Stock("주식A", "000010", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    Stock stockB = stockRepository.save(new Stock("주식B", "000011", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stockA, today, 12_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(stockB, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<RSRawScore> scores = calculator.calculateScores(List.of(stockA, stockB), today);

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
    Stock stock = stockRepository.save(new Stock("데이터없음", "999999", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);

    // when & then
    assertThatThrownBy(() -> calculator.calculateScores(List.of(stock), today))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("KOSPI 데이터가 없으면 예외가 발생한다")
  void calculateScores_whenKospiMissing_throwsException() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(new Stock("코스피없음", "888888", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when & then
    assertThatThrownBy(() -> calculator.calculateScores(List.of(stock), today))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
