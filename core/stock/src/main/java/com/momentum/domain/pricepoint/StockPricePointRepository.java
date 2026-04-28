package com.momentum.domain.pricepoint;

import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StockPricePointRepository {

  StockPricePoint save(StockPricePoint stockPricePoint);

  Optional<StockPricePoint> findTopByStockOrderByCreatedAtDesc(Stock stock);

  Optional<StockPricePoint> findLatestByStock(Stock stock);

  List<StockPricePoint> findTop3ByStockOrderByCreatedAtDesc(Long stockId);

  List<StockPricePoint> findTop4ByStockOrderByCreatedAtDesc(Long stockId);

  Optional<StockPricePoint> findUpperPricePoint(Instant currentBaseCreatedAt, long overPrice);

  Optional<StockPricePoint> findLineLowerPricePoint(Instant currentBaseCreatedAt, long lowerPrice);

  Optional<StockPricePoint> findPricePointNoBase(StockPricePointType stockPricePointType);

  List<StockPricePoint> findUnassignedPointsSinceBase(Instant lastBaseCreatedAt);

  List<StockPricePoint> saveAll(List<StockPricePoint> stockPricePoints);
}
