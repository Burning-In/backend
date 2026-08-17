package com.momentum.domain.base.entity;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockBaseLineStrengthTest {

  @Test
  @DisplayName("생성 직후에는 터치 횟수가 0이고 강도는 0이다(log(1)=0)")
  void createHasZeroStrength() {
    long currentVolume = 100L;
    long averageVolume = 100L;

    StockBaseLineStrength strength = StockBaseLineStrength.create(currentVolume, averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(strength.getTouchCount()).isZero();
      softly.assertThat(strength.getAccumulatedVolume()).isEqualTo(100L);
      softly.assertThat(strength.getStrength()).isEqualByComparingTo(BigDecimal.ZERO);
    });
  }

  @Test
  @DisplayName("터치하면 터치 횟수와 누적 거래량이 증가하고 강도가 재계산된다")
  void touchAccumulates() {
    long currentVolume = 100L;
    long averageVolume = 100L;
    long additionalVolume = 100L;
    // 정규화 거래량(200/100=2) * log(2) = 1.3862... → 소수 4자리 반올림
    BigDecimal expectedStrength = new BigDecimal("1.3863");
    StockBaseLineStrength strength = StockBaseLineStrength.create(currentVolume, averageVolume);

    strength.touch(additionalVolume, averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(strength.getTouchCount()).isEqualTo(1L);
      softly.assertThat(strength.getAccumulatedVolume()).isEqualTo(200L);
      softly.assertThat(strength.getStrength()).isEqualByComparingTo(expectedStrength);
    });
  }

  @Test
  @DisplayName("compareTo는 강도로 비교한다 — 터치가 쌓일수록 크고, 같으면 0이다")
  void compareToByStrength() {
    StockBaseLineStrength noTouch = StockBaseLineStrength.create(100L, 100L); // 강도 0
    StockBaseLineStrength oneTouch = StockBaseLineStrength.create(100L, 100L);
    oneTouch.touch(100L, 100L);
    StockBaseLineStrength twoTouches = StockBaseLineStrength.create(100L, 100L);
    twoTouches.touch(100L, 100L);
    twoTouches.touch(100L, 100L);
    StockBaseLineStrength sameAsNoTouch = StockBaseLineStrength.create(100L, 100L);

    assertSoftly(softly -> {
      softly.assertThat(oneTouch.compareTo(noTouch)).isPositive();
      softly.assertThat(twoTouches.compareTo(oneTouch)).isPositive();
      softly.assertThat(noTouch.compareTo(twoTouches)).isNegative();
      softly.assertThat(noTouch.compareTo(sameAsNoTouch)).isZero();
    });
  }
}
