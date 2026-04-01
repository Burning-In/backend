package com.momentum.domain.service;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.StockBase;
import com.momentum.domain.entity.indicator.StockLine;

public interface StockBaseService {

  StockBase createCandidate(Stock stock, StockLine triggerLine);
}
