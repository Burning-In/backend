package com.momentum.domain.stockcandle;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockCandleTrendTest {

  @Test
  @DisplayName("코드 문자열로 해당 추세를 찾는다")
  void getValueByCode() {
    assertSoftly(softly -> {
      softly.assertThat(StockCandleTrend.getValue("1")).isEqualTo(StockCandleTrend.UPPER_LIMIT);
      softly.assertThat(StockCandleTrend.getValue("2")).isEqualTo(StockCandleTrend.UP);
      softly.assertThat(StockCandleTrend.getValue("3")).isEqualTo(StockCandleTrend.FLAT);
      softly.assertThat(StockCandleTrend.getValue("4")).isEqualTo(StockCandleTrend.LOWER_LIMIT);
      softly.assertThat(StockCandleTrend.getValue("5")).isEqualTo(StockCandleTrend.DOWN);
    });
  }

  @Test
  @DisplayName("매칭되는 코드가 없으면 NONE을 반환한다")
  void getValueUnknownCodeReturnsNone() {
    assertSoftly(softly -> {
      softly.assertThat(StockCandleTrend.getValue("7")).isEqualTo(StockCandleTrend.NONE);
      softly.assertThat(StockCandleTrend.getValue("9999")).isEqualTo(StockCandleTrend.NONE);
    });
  }

  @Test
  @DisplayName("상한/상승은 상승 추세(isUpper)다")
  void isUpper() {
    assertSoftly(softly -> {
      softly.assertThat(StockCandleTrend.UPPER_LIMIT.isUpper()).isTrue();
      softly.assertThat(StockCandleTrend.UP.isUpper()).isTrue();
      softly.assertThat(StockCandleTrend.FLAT.isUpper()).isFalse();
      softly.assertThat(StockCandleTrend.DOWN.isUpper()).isFalse();
    });
  }

  @Test
  @DisplayName("하한/하락은 하락 추세(isLower)다")
  void isLower() {
    assertSoftly(softly -> {
      softly.assertThat(StockCandleTrend.LOWER_LIMIT.isLower()).isTrue();
      softly.assertThat(StockCandleTrend.DOWN.isLower()).isTrue();
      softly.assertThat(StockCandleTrend.FLAT.isLower()).isFalse();
      softly.assertThat(StockCandleTrend.UP.isLower()).isFalse();
    });
  }
}
