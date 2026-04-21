package com.momentum.domain.entity;

public enum StockRegime {
  BREAKOUT_START,      // 돌파시작
  BREAKOUT_READY,      // 돌파준비
  BREAKOUT_FAILED,     // 돌파실패
  DOWNSIDE_BREAK,      // 하방이탈
  UNDETERMINED;        // 방향미정
}
