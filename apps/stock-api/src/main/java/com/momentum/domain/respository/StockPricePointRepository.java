package com.momentum.domain.respository;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPricePointType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StockPricePointRepository {

  StockPricePoint save(StockPricePoint stockPricePoint);

  Optional<StockPricePoint> findTopByStockOrderByCreatedAtDesc(Stock stock);

  List<StockPricePoint> findTop3ByStockOrderByCreatedAtDesc(Stock stock);

  Optional<StockPricePoint> findUpperPricePoint(Instant currentBaseCreatedAt, long overPrice);

  Optional<StockPricePoint> findLineLowerPricePoint(Instant currentBaseCreatedAt, long lowerPrice);

  Optional<StockPricePoint> findPricePointNoBase(StockPricePointType stockPricePointType);

  List<StockPricePoint> findUnassignedPointsSinceBase(Instant lastBaseCreatedAt);
}
