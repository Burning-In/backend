package com.momentum.domain.pricepoint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockPricePointSlopeCalculatorTest {

  private static final BigDecimal ERROR_PERCENT = BigDecimal.valueOf(3.0);
  private static final LocalDate ANCHOR_DATE = LocalDate.of(2024, 1, 1);

  private final StockPricePointSlopeCalculator calculator = new StockPricePointSlopeCalculator();

  @Test
  @DisplayName("오차 범위만큼 벌어지므로 상한은 언제나 하한보다 작다")
  void calculateSlope_upperAlwaysLessThanLower() {
    // when
    SlopeResult result = calculator.calculateSlope(10_000L, ANCHOR_DATE, 10_500L, ANCHOR_DATE.plusDays(1),
        ERROR_PERCENT);

    // then
    assertThat(result.upper()).isLessThan(result.lower());
  }

  @Test
  @DisplayName("오차는 기준점 가격의 퍼센트로 환산된다")
  void calculateSlope_convertsErrorPercentIntoPriceOfAnchor() {
    // given
    long anchorPrice = 10_000L;
    long todayPrice = 11_000L;
    long errorPrice = 300L;

    // when
    SlopeResult result = calculator.calculateSlope(anchorPrice, ANCHOR_DATE, todayPrice, ANCHOR_DATE.plusDays(1),
        ERROR_PERCENT);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result.upper()).isEqualByComparingTo(BigDecimal.valueOf(1_000L - errorPrice));
      softly.assertThat(result.lower()).isEqualByComparingTo(BigDecimal.valueOf(1_000L + errorPrice));
    });
  }

  @Test
  @DisplayName("기울기는 기준점부터 지난 날수로 나눈 값이다")
  void calculateSlope_dividesByElapsedDays() {
    // when
    SlopeResult result = calculator.calculateSlope(10_000L, ANCHOR_DATE, 11_000L, ANCHOR_DATE.plusDays(5),
        ERROR_PERCENT);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result.upper()).isEqualByComparingTo(BigDecimal.valueOf(140));
      softly.assertThat(result.lower()).isEqualByComparingTo(BigDecimal.valueOf(260));
    });
  }

  @Test
  @DisplayName("하락 구간에서는 상한과 하한이 모두 음수가 된다")
  void calculateSlope_whenPriceFalls_returnsNegativeSlopes() {
    // when
    SlopeResult result = calculator.calculateSlope(11_000L, ANCHOR_DATE, 10_000L, ANCHOR_DATE.plusDays(1),
        ERROR_PERCENT);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result.upper()).isEqualByComparingTo(BigDecimal.valueOf(-1_330));
      softly.assertThat(result.lower()).isEqualByComparingTo(BigDecimal.valueOf(-670));
    });
  }

  @Test
  @DisplayName("날짜가 null이면 예외가 발생한다")
  void calculateSlope_withNullDate_throwsException() {
    assertSoftly(softly -> {
      softly.assertThatThrownBy(
              () -> calculator.calculateSlope(10_000L, null, 10_500L, ANCHOR_DATE.plusDays(1), ERROR_PERCENT))
          .isInstanceOf(IllegalArgumentException.class);
      softly.assertThatThrownBy(() -> calculator.calculateSlope(10_000L, ANCHOR_DATE, 10_500L, null, ERROR_PERCENT))
          .isInstanceOf(IllegalArgumentException.class);
    });
  }
}
