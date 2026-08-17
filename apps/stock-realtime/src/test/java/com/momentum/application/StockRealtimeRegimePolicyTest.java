package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_FAILED;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static com.momentum.sharedkernel.StockTrend.OTHER;
import static com.momentum.sharedkernel.StockTrend.UPTREND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import com.momentum.infrastructure.query.RealtimeRegimeRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockRealtimeRegimePolicyTest {

  private static final double THRESHOLD_PERCENT = 5.0;

  private static final long RESISTANCE_PRICE = 10_000L;
  private static final long SUPPORT_PRICE = 8_000L;
  // 저항선 상단 = 10_000 * 1.05, 지지선 하단 = 8_000 * 0.95
  private static final long RESISTANCE_UPPER_BOUND = 10_500L;
  private static final long SUPPORT_LOWER_BOUND = 7_600L;

  private final StockRealtimeRegimePolicy stockRealtimeRegimePolicy = new StockRealtimeRegimePolicy();

  @Test
  @DisplayName("판정 재료가 없으면 IllegalArgumentException")
  void throwsWhenSourceNull() {
    assertThatThrownBy(() -> stockRealtimeRegimePolicy.decide(10_000L, null, THRESHOLD_PERCENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재 베이스가 없으면 IllegalArgumentException")
  void throwsWhenBaseMissing() {
    RealtimeRegimeRow source =
        new RealtimeRegimeRow(BREAKOUT_READY.name(), UPTREND.name(), null, null, false, 9_000L);

    assertThatThrownBy(() -> stockRealtimeRegimePolicy.decide(10_000L, source, THRESHOLD_PERCENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("마지막 특이점이 없으면 IllegalArgumentException")
  void throwsWhenLastAnchorPointMissing() {
    RealtimeRegimeRow source = source(BREAKOUT_READY, UPTREND, false, null);

    assertThatThrownBy(() -> stockRealtimeRegimePolicy.decide(10_000L, source, THRESHOLD_PERCENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재가가 베이스의 지지선 하단(이탈임계)보다 낮으면 DOWNSIDE_BREAK")
  void downsideBreak() {
    RealtimeRegimeRow source = source(BREAKOUT_READY, UPTREND, false, 9_500L);

    StockRegime result =
        stockRealtimeRegimePolicy.decide(SUPPORT_LOWER_BOUND - 1, source, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(DOWNSIDE_BREAK);
  }

  @Test
  @DisplayName("돌파성공 상태에서 현재가가 직전 특이점보다 낮으면 BREAKOUT_FAILED")
  void breakoutFailed() {
    RealtimeRegimeRow source = source(BREAKOUT_SUCCESS, UPTREND, false, 9_500L);

    // 지지선 하단(7_600) 위이지만 직전 특이점(9_500)보다는 낮다
    StockRegime result = stockRealtimeRegimePolicy.decide(9_000L, source, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(BREAKOUT_FAILED);
  }

  @Test
  @DisplayName("상승추세이고 VCP이며 현재가가 저항선 상단을 넘으면 BREAKOUT_SUCCESS")
  void breakoutSuccess() {
    RealtimeRegimeRow source = source(BREAKOUT_READY, UPTREND, true, 9_000L);

    StockRegime result =
        stockRealtimeRegimePolicy.decide(RESISTANCE_UPPER_BOUND + 1, source, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("저항선 상단을 넘어도 VCP가 아니면 BREAKOUT_READY")
  void breakoutReady() {
    RealtimeRegimeRow source = source(BREAKOUT_READY, UPTREND, false, 9_000L);

    StockRegime result =
        stockRealtimeRegimePolicy.decide(RESISTANCE_UPPER_BOUND + 1, source, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("저항선 상단을 넘어도 상승추세가 아니면 UNKNOWN")
  void unknownWhenNotUptrend() {
    RealtimeRegimeRow source = source(BREAKOUT_READY, OTHER, true, 9_000L);

    StockRegime result =
        stockRealtimeRegimePolicy.decide(RESISTANCE_UPPER_BOUND + 1, source, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(UNKNOWN);
  }

  @Test
  @DisplayName("돌파/이탈 어느 조건에도 해당하지 않으면 UNKNOWN (판단 보류 — 기존 레짐 유지)")
  void unknownWhenNoConditionMatched() {
    RealtimeRegimeRow source = source(BREAKOUT_READY, UPTREND, false, 9_000L);

    // 지지선 하단(7_600)과 저항선 상단(10_500) 사이
    StockRegime result = stockRealtimeRegimePolicy.decide(10_000L, source, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(UNKNOWN);
  }

  private RealtimeRegimeRow source(StockRegime regime, StockTrend trend, boolean vcp, Long lastAnchorPointPrice) {
    return new RealtimeRegimeRow(regime.name(), trend.name(), SUPPORT_PRICE, RESISTANCE_PRICE, vcp,
        lastAnchorPointPrice);
  }
}
