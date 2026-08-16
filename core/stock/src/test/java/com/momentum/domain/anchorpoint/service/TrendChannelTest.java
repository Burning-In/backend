package com.momentum.domain.anchorpoint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrendChannelTest {

  @Test
  @DisplayName("상한이 하한보다 낮으면 아직 채널 안이다")
  void isOutOfChannel_whenUpperBelowLower_returnsFalse() {
    assertThat(channelOf("7", "8.5").isOutOfChannel()).isFalse();
  }

  @Test
  @DisplayName("상한과 하한이 같은 평행 상태는 아직 이탈이 아니다")
  void isOutOfChannel_whenBoundsEqual_returnsFalse() {
    assertThat(channelOf("7", "7").isOutOfChannel()).isFalse();
  }

  @Test
  @DisplayName("상한이 하한을 넘어서면 채널을 이탈한 것이다")
  void isOutOfChannel_whenUpperAboveLower_returnsTrue() {
    assertThat(channelOf("8.5", "7").isOutOfChannel()).isTrue();
  }

  @Test
  @DisplayName("상한이나 하한이 없는 채널은 만들 수 없다")
  void constructor_withNullBound_throwsException() {
    assertSoftly(softly -> {
      softly.assertThatThrownBy(() -> new TrendChannel(null, BigDecimal.ONE))
          .isInstanceOf(NullPointerException.class);
      softly.assertThatThrownBy(() -> new TrendChannel(BigDecimal.ONE, null))
          .isInstanceOf(NullPointerException.class);
    });
  }

  private static TrendChannel channelOf(String slopeUpperMax, String slopeLowerMin) {
    return new TrendChannel(new BigDecimal(slopeUpperMax), new BigDecimal(slopeLowerMin));
  }
}
