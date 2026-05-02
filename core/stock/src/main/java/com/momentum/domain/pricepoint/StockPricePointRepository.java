package com.momentum.domain.pricepoint;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.infrastructure.pricepoint.dto.RecentPricePoints;
import java.util.List;
import java.util.Optional;

public interface StockPricePointRepository {

  StockPricePoint save(StockPricePoint stockPricePoint);

  Optional<StockPricePoint> findLastStockPricePoint(Stock stock);

  Optional<StockPricePoint> findLatestByStock(Stock stock);

  Optional<RecentPricePoints> findRecentPricePoints(Long stockId);

  Optional<StockPricePoint> findHighPricePoint(StockBase currentBase, long overPrice);

  Optional<StockPricePoint> findLowPricePoint(StockBase currentBase, long lowerPrice);

  Optional<StockPricePoint> findLastPricePointWithoutBase(StockPricePointType stockPricePointType);

  List<StockPricePoint> findUnassignedPointsSinceBase(StockBase currentBase);

  List<StockPricePoint> saveAll(List<StockPricePoint> stockPricePoints);
}
