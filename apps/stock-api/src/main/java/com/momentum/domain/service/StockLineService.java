package com.momentum.domain.service;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.StockLine;

public interface StockLineService {

  StockLine determineResistance(Stock stock, StockCandle candle, double v);
}
