package com.momentum.domain.relativestrength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class KospiRelativeStrengthServiceTest {

  @Autowired
  private KospiRelativeStrengthService service;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private KospiRepository kospiRepository;

  @Autowired
  private RsTestSupport rsTestSupport;

  @Test
  @DisplayName("여러 주식의 RS 등급은 원점수 순위대로 배분된다")
  void create_withMultipleStocks_assignsRatingsBetweenOneAndNinetyNine() {
    // given
    LocalDate today = LocalDate.now();
    Stock stockA = stockRepository.save(Stock.of("주식A", "000140", StockRegime.UNKNOWN, StockTrend.UPTREND));
    Stock stockB = stockRepository.save(Stock.of("주식B", "000150", StockRegime.UNKNOWN, StockTrend.UPTREND));
    Stock stockC = stockRepository.save(Stock.of("주식C", "000180", StockRegime.UNKNOWN, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stockA, today, 8_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(stockB, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(stockC, today, 13_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<KospiRelativeStrength> result = service.create(today);

    // then
    // 등급 = round(rank/3 * 98) + 1 → 0, 1, 2위가 각각 1, 34, 66등급이 된다.
    assertThat(result).hasSize(3);
    assertThat(result).extracting(KospiRelativeStrength::getRsScore)
        .containsExactlyInAnyOrder(1, 34, 66);
    assertThat(ratingOf(result, stockA)).isEqualTo(1);
    assertThat(ratingOf(result, stockC)).isEqualTo(66);
  }

  @Test
  @DisplayName("오늘 날짜의 KOSPI 데이터가 없으면 예외가 발생한다")
  void create_whenTodayKospiMissing_throwsException() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(Stock.of("코스피없음", "000210", StockRegime.UNKNOWN, StockTrend.UPTREND));

    kospiRepository.save(new Kospi(2500L, today.minusDays(1)));
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when & then
    assertThatThrownBy(() -> service.create(today))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("RS 등급이 80 이상이면 UPTREND로, 미만이면 OTHER로 갱신된다")
  void create_updatesStockTrendByRsRating() {
    // given
    LocalDate today = LocalDate.now();
    rsTestSupport.setupKospi(today, 2500L);

    // 6개 종목 → 등급 = round(rank/6 * 98) + 1 이므로 최강(rank 5)만 83점으로 80을 넘는다.
    // 최약 종목은 UPTREND로 시작시켜, 강세가 아니면 OTHER로 내려가는지도 함께 검증한다.
    Stock weakest = saveStock("000300", StockTrend.UPTREND);
    Stock second = saveStock("000310", StockTrend.OTHER);
    Stock third = saveStock("000320", StockTrend.OTHER);
    Stock fourth = saveStock("000330", StockTrend.OTHER);
    Stock fifth = saveStock("000340", StockTrend.OTHER);
    Stock strongest = saveStock("000350", StockTrend.OTHER);

    setupFlatBaseCandles(weakest, today, 8_000L);
    setupFlatBaseCandles(second, today, 9_000L);
    setupFlatBaseCandles(third, today, 10_000L);
    setupFlatBaseCandles(fourth, today, 11_000L);
    setupFlatBaseCandles(fifth, today, 12_000L);
    setupFlatBaseCandles(strongest, today, 13_000L);

    // when
    List<KospiRelativeStrength> result = service.create(today);

    // then
    assertThat(ratingOf(result, strongest)).isEqualTo(83);
    assertThat(ratingOf(result, fifth)).isEqualTo(66);
    assertThat(strongest.getStockTrend()).isEqualTo(StockTrend.UPTREND);
    assertThat(fifth.getStockTrend()).isEqualTo(StockTrend.OTHER);
    assertThat(weakest.getStockTrend()).isEqualTo(StockTrend.OTHER);
  }

  @Test
  @DisplayName("이미 UPTREND인 종목이 계속 강세면 UPTREND를 유지한다")
  void create_whenAlreadyUptrendAndStillStrong_keepsUptrend() {
    // given
    LocalDate today = LocalDate.now();
    rsTestSupport.setupKospi(today, 2500L);

    // 6개 종목 중 최강(rank 5)만 83점으로 80을 넘는다. 이 종목을 UPTREND로 시작시킨다.
    Stock weakest = saveStock("000400", StockTrend.OTHER);
    Stock second = saveStock("000410", StockTrend.OTHER);
    Stock third = saveStock("000420", StockTrend.OTHER);
    Stock fourth = saveStock("000430", StockTrend.OTHER);
    Stock fifth = saveStock("000440", StockTrend.OTHER);
    Stock strongest = saveStock("000450", StockTrend.UPTREND);

    setupFlatBaseCandles(weakest, today, 8_000L);
    setupFlatBaseCandles(second, today, 9_000L);
    setupFlatBaseCandles(third, today, 10_000L);
    setupFlatBaseCandles(fourth, today, 11_000L);
    setupFlatBaseCandles(fifth, today, 12_000L);
    setupFlatBaseCandles(strongest, today, 13_000L);

    // when
    List<KospiRelativeStrength> result = service.create(today);

    // then
    assertThat(ratingOf(result, strongest)).isEqualTo(83);
    assertThat(strongest.getStockTrend()).isEqualTo(StockTrend.UPTREND);
  }

  private Stock saveStock(String code, StockTrend trend) {
    return stockRepository.save(Stock.of("종목" + code, code, StockRegime.UNKNOWN, trend));
  }

  // 과거 4개 시점을 10_000으로 고정해, 당일 종가만으로 RS 원점수 순위가 갈리게 한다.
  private void setupFlatBaseCandles(Stock stock, LocalDate today, long todayPrice) {
    rsTestSupport.setupCandles(stock, today, todayPrice, 10_000L, 10_000L, 10_000L, 10_000L);
  }

  private int ratingOf(List<KospiRelativeStrength> result, Stock stock) {
    return result.stream()
        .filter(rs -> rs.getStock().getId().equals(stock.getId()))
        .findFirst()
        .orElseThrow()
        .getRsScore();
  }
}
