package com.momentum.domain.score;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MomentumScoreTest {

  @Test
  @DisplayName("모멘텀 = (현재가 - 1년전가) / 1년전가")
  void calculatePositiveMomentum() {
    MomentumScore momentumScore = MomentumScore.calculate(12_000L, 10_000L);

    // (12000 - 10000) / 10000 = 0.2
    assertThat(momentumScore.getValue()).isEqualByComparingTo(new BigDecimal("0.2"));
  }

  @Test
  @DisplayName("현재가가 1년전가보다 낮으면 모멘텀은 음수다")
  void calculateNegativeMomentum() {
    MomentumScore momentumScore = MomentumScore.calculate(9_000L, 10_000L);

    // (9000 - 10000) / 10000 = -0.1
    assertThat(momentumScore.getValue()).isEqualByComparingTo(new BigDecimal("-0.1"));
  }
}
