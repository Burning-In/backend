package com.momentum.domain.service;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockLine;

public interface StockBaseService {

  StockBase createCandidate(Stock stock, StockLine triggerLine);

  void evaluateBase(Stock stock, StockLine firstLineAfterCandidate);
}
