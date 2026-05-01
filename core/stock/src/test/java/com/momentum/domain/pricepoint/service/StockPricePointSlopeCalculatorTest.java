package com.momentum.domain.pricepoint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.pricepoint.service.StockPricePointSlopeCalculator.SlopeResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockPricePointSlopeCalculatorTest {

  private StockPricePointSlopeCalculator calculator;
  private static final BigDecimal ERROR = BigDecimal.valueOf(3.0);

  @BeforeEach
  void setUp() {
    calculator = new StockPricePointSlopeCalculator();
  }

  @Test
  @DisplayName("정상 계산 - SU < SL 항상 성립")
  void calculateSlope_suAlwaysLessThanSl() {
    // given
    long pivotPrice = 10000L;
    long todayPrice = 10500L;
    LocalDate pivotDate = LocalDate.of(2024, 1, 1);
    LocalDate todayDate = LocalDate.of(2024, 1, 2); // 1일 차이

    // when
    SlopeResult result = calculator.calculateSlope(pivotPrice, pivotDate, todayPrice, todayDate, ERROR);

    // then
    // SU = (500 - 3) / 1 = 497.0
    // SL = (500 + 3) / 1 = 503.0
    // SU < SL 항상 성립
    assertThat(result.upper()).isLessThan(result.lower());
  }

  @Test
  @DisplayName("정상 계산 - 날짜 차이 여러 날")
  void calculateSlope_multipleDays() {
    // given
    long pivotPrice = 10000L;
    long todayPrice = 11000L;
    LocalDate pivotDate = LocalDate.of(2024, 1, 1);
    LocalDate todayDate = LocalDate.of(2024, 1, 6); // 5일 차이

    // when
    SlopeResult result = calculator.calculateSlope(pivotPrice, pivotDate, todayPrice, todayDate, ERROR);

    // then
    // SU = (1000 - 3) / 5 = 199.4
    // SL = (1000 + 3) / 5 = 200.6
    BigDecimal expectedSU = BigDecimal.valueOf(997).divide(BigDecimal.valueOf(5), 10, java.math.RoundingMode.HALF_UP);
    BigDecimal expectedSL = BigDecimal.valueOf(1003).divide(BigDecimal.valueOf(5), 10, java.math.RoundingMode.HALF_UP);

    assertThat(result.upper()).isEqualByComparingTo(expectedSU);
    assertThat(result.lower()).isEqualByComparingTo(expectedSL);
  }

  @Test
  @DisplayName("하락 구간 - 음수 기울기")
  void calculateSlope_negativeSlope() {
    // given
    long pivotPrice = 11000L;
    long todayPrice = 10000L; // 하락
    LocalDate pivotDate = LocalDate.of(2024, 1, 1);
    LocalDate todayDate = LocalDate.of(2024, 1, 2);

    // when
    SlopeResult result = calculator.calculateSlope(pivotPrice, pivotDate, todayPrice, todayDate, ERROR);

    // then
    // SU = (-1000 - 3) / 1 = -1003.0
    // SL = (-1000 + 3) / 1 = -997.0
    assertThat(result.upper()).isLessThan(result.lower());
    assertThat(result.upper().compareTo(BigDecimal.ZERO)).isLessThan(0);
    assertThat(result.lower().compareTo(BigDecimal.ZERO)).isLessThan(0);
  }

  @Test
  @DisplayName("pivotDate가 null이면 예외 발생")
  void calculateSlope_nullPivotDate() {
    assertThatThrownBy(() ->
        calculator.calculateSlope(10000L, null, 10500L, LocalDate.of(2024, 1, 2), ERROR)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("todayDate가 null이면 예외 발생")
  void calculateSlope_nullTodayDate() {
    assertThatThrownBy(() ->
        calculator.calculateSlope(10000L, LocalDate.of(2024, 1, 1), 10500L, null, ERROR)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
