package com.momentum.domain.service.impl;

import com.momentum.domain.entity.StockDailyCandle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockDailyCandleServiceImpl {

  // 외부에서 받아오고, 캔들데이터를 받았으면, 일단 저장하는데,
  // 서비스 전일대비 +인지 -인지 판단
  // DTO로 받아서 넣어줘야함
  @Transactional
  public StockDailyCandle create() {

    return null;
  }
}
