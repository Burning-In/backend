package com.momentum.domain.rs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
class KOSPIRelativeStrengthServiceTest {

  @Autowired
  private KOSPIRelativeStrengthService service;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private KOSPIRepository kospiRepository;

  @Autowired
  private RsTestSupport rsTestSupport;

  @Test
  @DisplayName("주식이 1개일 때 RS 등급은 1이다")
  void create_withSingleStock_returnsRatingOne() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(new Stock("단일주식", "111111", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<KOPSIRelativeStrength> result = service.create(today);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("여러 주식의 RS 등급은 1~99 사이에서 배분된다")
  void create_withMultipleStocks_assignsRatingsBetweenOneAndNinetyNine() {
    // given
    LocalDate today = LocalDate.now();
    Stock stockA = stockRepository.save(new Stock("주식A", "222222", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    Stock stockB = stockRepository.save(new Stock("주식B", "333333", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    Stock stockC = stockRepository.save(new Stock("주식C", "444444", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(stockA, today, 8_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(stockB, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(stockC, today, 13_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<KOPSIRelativeStrength> result = service.create(today);

    // then
    assertThat(result).hasSize(3);
  }

  @Test
  @DisplayName("오늘 날짜의 KOSPI 데이터가 없으면 예외가 발생한다")
  void create_whenTodayKospiMissing_throwsException() {
    // given
    LocalDate today = LocalDate.now();
    Stock stock = stockRepository.save(new Stock("코스피없음", "555555", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    kospiRepository.save(new KOSPI(2500L, today.minusDays(1)));
    rsTestSupport.setupCandles(stock, today, 10_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when & then
    assertThatThrownBy(() -> service.create(today))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("2개 주식일 때 가장 약한 주식은 1등급, 가장 강한 주식은 50등급이다")
  void create_withTwoStocks_assignsCorrectRatings() {
    // given
    LocalDate today = LocalDate.now();
    Stock weakStock = stockRepository.save(new Stock("약한주식", "666666", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    Stock strongStock = stockRepository.save(new Stock("강한주식", "777777", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));

    rsTestSupport.setupKospi(today, 2500L);
    rsTestSupport.setupCandles(weakStock, today, 8_000L, 10_000L, 10_000L, 10_000L, 10_000L);
    rsTestSupport.setupCandles(strongStock, today, 13_000L, 10_000L, 10_000L, 10_000L, 10_000L);

    // when
    List<KOPSIRelativeStrength> result = service.create(today);

    // then
    // 정렬 후 i=0 → rsRating=1, i=1 → rsRating=round(1/2 * 98)+1=50
    assertThat(result).hasSize(2);
    List<Integer> ratings = result.stream()
        .map(KOPSIRelativeStrength::getRsScore)
        .sorted()
        .toList();
    assertThat(ratings).containsExactly(1, 50);
  }
}
