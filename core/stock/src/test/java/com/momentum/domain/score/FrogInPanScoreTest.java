package com.momentum.domain.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrogInPanScoreTest {

  @Test
  @DisplayName("상승/하락일수를 세고, 양수 모멘텀이면 FIP = +(상승일-하락일)/252")
  void calculateWithPositiveMomentum() {
    BigDecimal positiveMomentum = new BigDecimal("0.5");
    // 최신순 [110, 100, 90] → 상승 2일, 하락 0일
    List<Long> closePrices = List.of(110L, 100L, 90L);

    FrogInPanScore frogInPanScore = FrogInPanScore.calculate(positiveMomentum, closePrices);

    assertSoftly(softly -> {
      softly.assertThat(frogInPanScore.getUpDays()).isEqualTo(2);
      softly.assertThat(frogInPanScore.getDownDays()).isEqualTo(0);
      // sign(+) * (2/252 - 0/252) = 0.007937
      softly.assertThat(frogInPanScore.getValue()).isEqualByComparingTo(new BigDecimal("0.007937"));
    });
  }

  @Test
  @DisplayName("음수 모멘텀이면 FIP의 부호가 반전된다")
  void calculateWithNegativeMomentum() {
    BigDecimal negativeMomentum = new BigDecimal("-0.5");
    List<Long> closePrices = List.of(110L, 100L, 90L); // 상승 2일 → FIP 크기는 동일

    FrogInPanScore frogInPanScore = FrogInPanScore.calculate(negativeMomentum, closePrices);

    assertThat(frogInPanScore.getValue()).isEqualByComparingTo(new BigDecimal("-0.007937"));
  }

  @Test
  @DisplayName("상승일과 하락일이 같으면 FIP는 0이다")
  void calculateNetZero() {
    BigDecimal momentum = new BigDecimal("0.5");
    // 최신순 [100, 110, 105] → 하락 1일, 상승 1일
    List<Long> closePrices = List.of(100L, 110L, 105L);

    FrogInPanScore frogInPanScore = FrogInPanScore.calculate(momentum, closePrices);

    assertSoftly(softly -> {
      softly.assertThat(frogInPanScore.getUpDays()).isEqualTo(1);
      softly.assertThat(frogInPanScore.getDownDays()).isEqualTo(1);
      softly.assertThat(frogInPanScore.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    });
  }
}
