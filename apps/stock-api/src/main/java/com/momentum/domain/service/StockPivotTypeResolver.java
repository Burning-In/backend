package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPivotType;
import com.momentum.domain.entity.indicator.price.StockPivot;
import com.momentum.domain.respository.StockPivotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPivotTypeResolver {

  private final StockPivotRepository stockPivotRepository;

  public void resolveType(StockPivot current) {
    List<StockPivot> recentPivots = stockPivotRepository.findTop3ByStockOrderByCreatedAtDesc(current.getStock());
    if (recentPivots.size() < 3) {
      return;
    }

    StockPivot middle = recentPivots.get(1);
    StockPivot oldest = recentPivots.get(2);

    long oldestPrice = oldest.getPrice();
    long middlePrice = middle.getPrice();
    long currentPrice = current.getPrice();

    if (middlePrice > oldestPrice && middlePrice > currentPrice) {
      middle.updateType(StockPivotType.PIVOT_HIGH);
    } else if (middlePrice < oldestPrice && middlePrice < currentPrice) {
      middle.updateType(StockPivotType.PIVOT_LOW);
    }

    stockPivotRepository.save(middle);
  }
}
