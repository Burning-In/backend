package com.momentum.domain.base.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockBaseVcpTest {

  @Test
  @DisplayName("변동성 이력이 1개면 VCP로 판단하지 않는다")
  void singleHistoryIsNotVcp() {
    StockBaseVcp vcp = new StockBaseVcp();

    vcp.updateVcp(List.of(100L));

    assertThat(vcp.isVcp()).isFalse();
  }

  @Test
  @DisplayName("입력이 null이면 예외 없이 VCP로 판단하지 않는다")
  void nullHistoryIsNotVcp() {
    StockBaseVcp vcp = new StockBaseVcp();

    vcp.updateVcp(null);

    assertThat(vcp.isVcp()).isFalse();
  }

  @Test
  @DisplayName("변동성 이력이 2개이고 직전이 직후보다 크면(변동성 축소) VCP다")
  void twoHistoriesShrinkingIsVcp() {
    StockBaseVcp vcp = new StockBaseVcp();

    vcp.updateVcp(List.of(100L, 50L));

    assertThat(vcp.isVcp()).isTrue();
  }

  @Test
  @DisplayName("변동성 이력이 2개이고 직전이 직후보다 작으면(변동성 확대) VCP가 아니다")
  void twoHistoriesExpandingIsNotVcp() {
    StockBaseVcp vcp = new StockBaseVcp();

    vcp.updateVcp(List.of(50L, 100L));

    assertThat(vcp.isVcp()).isFalse();
  }

  @Test
  @DisplayName("변동성 이력이 3개 이상이고 이동평균 추세 기울기가 음수(감소)면 VCP다")
  void multipleHistoriesDecreasingSlopeIsVcp() {
    StockBaseVcp vcp = new StockBaseVcp();

    vcp.updateVcp(List.of(100L, 80L, 60L, 40L));

    assertThat(vcp.isVcp()).isTrue();
  }

  @Test
  @DisplayName("변동성 이력이 3개 이상이고 이동평균 추세 기울기가 양수(증가)면 VCP가 아니다")
  void multipleHistoriesIncreasingSlopeIsNotVcp() {
    StockBaseVcp vcp = new StockBaseVcp();

    vcp.updateVcp(List.of(40L, 60L, 80L, 100L));

    assertThat(vcp.isVcp()).isFalse();
  }
}
