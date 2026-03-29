package com.momentum.domain.service.impl;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockDailyCandle;
import org.springframework.stereotype.Service;

@Service
public class StockPivotServiceImpl {

  private static final double PIVOT_THRESHOLD_PERCENT = 4.0;

  // 피봇인지는 현재에서 이틀후에 판단합니다.
  // 서비스 로직에서 이틀전 데이터를 보고, -++이고, 변화량이 +4%이면 피봇

  // # 저점생성, -++, 전날 비교해서 +인가 -인가
  // 저점판단 -> targetDate기준으로 가야되네 이거
  // 높 낮 높(만약에 4%상승이아니라면) 낮(여기서 채워야한다)
  // 높 낮 높(만약에 4%상승이라면)


  // # 고점생성 +--
  // 확실한 고점 -> 양옆이 +- 4%여야하나?
  // 낮 높 낮(만약에 4%하락이 아니라면) 낮
  // 낮 높 낮(만약에 4%하락이라면)
  public StockDailyCandle determinePivot(Stock stock) {

    return null;
  }
}
