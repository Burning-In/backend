package com.momentum.domain.stock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackedStockTest {

  @Test
  @DisplayName("코드 문자열로 해당 TrackedStock을 찾는다")
  void getCode() {
    assertSoftly(softly -> {
      softly.assertThat(TrackedStock.fromCode("005930")).isEqualTo(TrackedStock.삼성전자);
      softly.assertThat(TrackedStock.fromCode("000660")).isEqualTo(TrackedStock.SK하이닉스);
    });
  }

  @Test
  @DisplayName("정의되지 않은 코드면 IllegalArgumentException을 던진다")
  void getCodeThrowsWhenUnknown() {
    String unknownCode = "000000";

    assertThatThrownBy(() -> TrackedStock.fromCode(unknownCode))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(unknownCode);
  }
}
