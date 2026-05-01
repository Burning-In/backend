package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stock.Stock;
import com.momentum.infrastructure.pricepoint.dto.RecentPricePoints;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointTypeDecider {

  private static final BigDecimal FLAT_THRESHOLD = BigDecimal.valueOf(1.0);

  private final StockPricePointRepository stockPricePointRepository;
  private final StockPricePointTypeFlatResolver flatResolver;
  private final StockPricePointTypeNormalResolver normalResolver;

  public List<StockPricePoint> resolveType(Stock stock) {
    RecentPricePoints points = stockPricePointRepository.findRecentPricePoints(stock.getId())
        .orElseThrow(() -> new IllegalArgumentException("PricePoint가 3개 미만입니다. stockId: " + stock.getId()));

    if (points.point1().isFlat(points.point2(), FLAT_THRESHOLD)) {
      return flatResolver.resolve(points);
    }
    return normalResolver.resolve(points);
  }
}
