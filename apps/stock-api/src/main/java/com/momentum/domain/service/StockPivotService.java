package com.momentum.domain.service;

import com.momentum.domain.entity.StockCandle;
import java.time.LocalDate;

public interface StockPivotService {

  StockCandle determineDailyPivot(String stockCode, LocalDate tradeDate);
}
