package com.momentum.domain.respository;

import com.momentum.domain.entity.StockTick;

public interface StockTickRepository {

  StockTick save(StockTick stockTick);
}
