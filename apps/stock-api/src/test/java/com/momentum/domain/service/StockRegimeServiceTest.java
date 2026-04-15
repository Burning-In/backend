package com.momentum.domain.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockRegimeServiceTest {

  // -------------------------------
  // 1. BREAKOUT
  // -------------------------------
  @Test
  @DisplayName("상승추세 + 저항 돌파 + 체결강도 증가 → BREAKOUT")
  void processTick_breakout() {

  }

  // -------------------------------
  // 2. FAILED_BREAKOUT
  // -------------------------------
  @Test
  @DisplayName("저항 아래 하락 + 위에서 약함 → FAILED_BREAKOUT")
  void processTick_failedBreakout() {

  }

  // -------------------------------
  // 3. BREAKDOWN
  // -------------------------------
  @Test
  @DisplayName("지지선 붕괴 + 아래에서 약함 → BREAKDOWN")
  void processTick_breakdown() {

  }

  @Test
  @DisplayName("종가가 저항 돌파 → BREAKOUT")
  void finalize_breakout() {

  }

  @Disabled
  @Test
  @DisplayName("수렴 상태 + 저항 아래 → BREAKOUT_CANDIDATE")
  void finalize_candidate_vcp() {

  }

  @Test
  @DisplayName("저항 아래 유지 → FAILED_BREAKOUT")
  void finalize_failedBreakout() {

  }

  @Test
  @DisplayName("지지선 붕괴 → BREAKDOWN")
  void finalize_breakdown() {

  }
}
