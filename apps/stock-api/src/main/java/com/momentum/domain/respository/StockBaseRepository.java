package com.momentum.domain.respository;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockBaseType;
import java.util.Optional;

public interface StockBaseRepository {

  StockBase save(StockBase candidate);

  Optional<StockBase> findLastBase(Long stockId, StockBaseType stockBaseType);
}
