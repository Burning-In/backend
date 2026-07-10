package com.momentum.domain.base.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockBaseLinePriceTest {

  @Test
  @DisplayName("상단 경계는 가격에 임계치(%)만큼 더한 값이다")
  void upperBound() {
    long price = 10_000L;
    double thresholdPercent = 5.0;
    StockBaseLinePrice linePrice = new StockBaseLinePrice(price);

    assertThat(linePrice.getUpperBound(thresholdPercent)).isEqualTo(10_500L);
  }

  @Test
  @DisplayName("하단 경계는 가격에서 임계치(%)만큼 뺀 값이다")
  void lowerBound() {
    long price = 10_000L;
    double thresholdPercent = 5.0;
    StockBaseLinePrice linePrice = new StockBaseLinePrice(price);

    assertThat(linePrice.getLowerBound(thresholdPercent)).isEqualTo(9_500L);
  }

  @Test
  @DisplayName("가격 차이 비율이 임계치 이하이면 임계 범위 안이다")
  void withinThreshold() {
    long price = 10_000L;
    long withinPrice = 10_500L;
    double thresholdRatio = 0.05;
    StockBaseLinePrice linePrice = new StockBaseLinePrice(price);

    assertThat(linePrice.isWithinThreshold(withinPrice, thresholdRatio)).isTrue();
  }

  @Test
  @DisplayName("가격 차이 비율이 임계치를 초과하면 임계 범위 밖이다")
  void outsideThreshold() {
    long price = 10_000L;
    long outsidePrice = 10_600L;
    double thresholdRatio = 0.05;
    StockBaseLinePrice linePrice = new StockBaseLinePrice(price);

    assertThat(linePrice.isWithinThreshold(outsidePrice, thresholdRatio)).isFalse();
  }
}
