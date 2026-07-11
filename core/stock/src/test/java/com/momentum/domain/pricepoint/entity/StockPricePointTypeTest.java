package com.momentum.domain.pricepoint.entity;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.ASCENDING;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.DESCENDING;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.FLAT;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.LOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockPricePointTypeTest {

  private final Stock stock = new Stock("종목", "000001", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("직전 값이 없고 target이 직후보다 크면 HIGH")
  void resolveFirstNullPivotHigh() {
    StockPricePoint target = point(100L);
    StockPricePoint next = point(90L);

    assertThat(StockPricePointType.classify(null, target, next)).isEqualTo(HIGH);
  }

  @Test
  @DisplayName("직전 값이 없고 target이 직후보다 작으면 LOW")
  void resolveFirstNullPivotLow() {
    StockPricePoint target = point(90L);
    StockPricePoint next = point(100L);

    assertThat(StockPricePointType.classify(null, target, next)).isEqualTo(LOW);
  }

  @Test
  @DisplayName("직전 값이 없고 target과 직후가 같으면 FLAT")
  void resolveFirstNullFlat() {
    StockPricePoint target = point(100L);
    StockPricePoint next = point(100L);

    assertThat(StockPricePointType.classify(null, target, next)).isEqualTo(FLAT);
  }

  @Test
  @DisplayName("target이 직전·직후보다 모두 크면 HIGH")
  void resolvePivotHigh() {
    StockPricePoint previous = point(80L);
    StockPricePoint target = point(120L);
    StockPricePoint next = point(90L);

    assertThat(StockPricePointType.classify(previous, target, next)).isEqualTo(HIGH);
  }

  @Test
  @DisplayName("target이 직전·직후보다 모두 작으면 LOW")
  void resolvePivotLow() {
    StockPricePoint previous = point(120L);
    StockPricePoint target = point(80L);
    StockPricePoint next = point(100L);

    assertThat(StockPricePointType.classify(previous, target, next)).isEqualTo(LOW);
  }

  @Test
  @DisplayName("직전 < target < 직후이면 ASCENDING")
  void resolveAscending() {
    StockPricePoint previous = point(80L);
    StockPricePoint target = point(90L);
    StockPricePoint next = point(100L);

    assertThat(StockPricePointType.classify(previous, target, next)).isEqualTo(ASCENDING);
  }

  @Test
  @DisplayName("직전 > target > 직후이면 DESCENDING")
  void resolveDescending() {
    StockPricePoint previous = point(100L);
    StockPricePoint target = point(90L);
    StockPricePoint next = point(80L);

    assertThat(StockPricePointType.classify(previous, target, next)).isEqualTo(DESCENDING);
  }

  @Test
  @DisplayName("추세도 특이점도 아닌 경우 FLAT")
  void resolveFlat() {
    StockPricePoint previous = point(80L);
    StockPricePoint target = point(100L);
    StockPricePoint next = point(100L);

    assertThat(StockPricePointType.classify(previous, target, next)).isEqualTo(FLAT);
  }

  @Test
  @DisplayName("FLAT/UNKNOWN/ASCENDING/DESCENDING은 비(非)특이점이다")
  void isNonPivotTrue() {
    assertSoftly(softly -> {
      softly.assertThat(StockPricePointType.isNonPivot(pointOfType(FLAT))).isTrue();
      softly.assertThat(StockPricePointType.isNonPivot(pointOfType(StockPricePointType.UNKNOWN))).isTrue();
      softly.assertThat(StockPricePointType.isNonPivot(pointOfType(ASCENDING))).isTrue();
      softly.assertThat(StockPricePointType.isNonPivot(pointOfType(DESCENDING))).isTrue();
    });
  }

  @Test
  @DisplayName("HIGH/LOW는 특이점이다")
  void isNonPivotFalse() {
    assertSoftly(softly -> {
      softly.assertThat(StockPricePointType.isNonPivot(pointOfType(HIGH))).isFalse();
      softly.assertThat(StockPricePointType.isNonPivot(pointOfType(LOW))).isFalse();
    });
  }

  @Test
  @DisplayName("가격 차이 비율이 flat 임계치 이하이면 flat이다")
  void isFlatWhenWithinThreshold() {
    StockPricePoint first = point(10_000L);
    StockPricePoint second = point(10_100L); // 1% 차이
    double flatThreshold = 2.0;

    assertThat(StockPricePointType.isFlat(first, second, flatThreshold)).isTrue();
  }

  @Test
  @DisplayName("가격 차이 비율이 flat 임계치를 초과하면 flat이 아니다")
  void isNotFlatWhenExceedsThreshold() {
    StockPricePoint first = point(10_000L);
    StockPricePoint second = point(10_100L); // 1% 차이
    double strictThreshold = 0.5;

    assertThat(StockPricePointType.isFlat(first, second, strictThreshold)).isFalse();
  }

  private StockPricePoint point(long price) {
    return new StockPricePoint(price, 100L, LocalDate.now(), FLAT, null, stock);
  }

  private StockPricePoint pointOfType(StockPricePointType type) {
    return new StockPricePoint(10_000L, 100L, LocalDate.now(), type, null, stock);
  }
}
