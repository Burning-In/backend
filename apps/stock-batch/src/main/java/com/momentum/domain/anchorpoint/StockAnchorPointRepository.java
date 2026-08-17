package com.momentum.domain.anchorpoint;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.dto.RecentAnchorPoints;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;

public interface StockAnchorPointRepository {

  StockAnchorPoint save(StockAnchorPoint stockAnchorPoint);

  Optional<StockAnchorPoint> findLastStockAnchorPoint(Stock stock);

  Optional<StockAnchorPoint> findLatestByStock(Stock stock);

  Optional<RecentAnchorPoints> findRecentAnchorPoints(Long stockId);

  Optional<StockAnchorPoint> findHighAnchorPoint(StockBase currentBase, long overPrice);

  Optional<StockAnchorPoint> findLowAnchorPoint(StockBase currentBase, long lowerPrice);

  Optional<StockAnchorPoint> findLastAnchorPointWithoutBase(Stock stock, StockAnchorPointType stockAnchorPointType);

  List<StockAnchorPoint> findUnassignedPointsSinceBase(StockBase currentBase);

  List<StockAnchorPoint> saveAll(List<StockAnchorPoint> stockAnchorPoints);
}
