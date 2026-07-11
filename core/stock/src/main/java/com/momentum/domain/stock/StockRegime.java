package com.momentum.domain.stock;

public enum StockRegime {
  BREAKOUT_SUCCESS,
  BREAKOUT_READY,
  BREAKOUT_FAILED,
  DOWNSIDE_BREAK,
  DIRECTION_UNDETERMINED, // 방향 미정 (확정된 상태)
  UNKNOWN; // 실시간 판단 보류 — 기존 레짐 유지
}
