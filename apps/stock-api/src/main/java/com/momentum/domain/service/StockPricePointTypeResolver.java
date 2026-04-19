package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPricePointType;
import com.momentum.domain.respository.StockPricePointRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointTypeResolver {

  private final StockPricePointRepository stockPricePointRepository;

  public void resolveType(StockPricePoint current) {
    List<StockPricePoint> recentPivots = stockPricePointRepository.findTop3ByStockOrderByCreatedAtDesc(current.getStock());
    if (recentPivots.size() < 3) {
      return;
    }

    StockPricePoint middle = recentPivots.get(1);
    StockPricePoint oldest = recentPivots.get(2);

    long oldestPrice = oldest.getPrice();
    long middlePrice = middle.getPrice();
    long currentPrice = current.getPrice();

    if (middlePrice > oldestPrice && middlePrice > currentPrice) {
      middle.updateType(StockPricePointType.PIVOT_HIGH);
    } else if (middlePrice < oldestPrice && middlePrice < currentPrice) {
      middle.updateType(StockPricePointType.PIVOT_LOW);
    }

    stockPricePointRepository.save(middle);
  }
}
