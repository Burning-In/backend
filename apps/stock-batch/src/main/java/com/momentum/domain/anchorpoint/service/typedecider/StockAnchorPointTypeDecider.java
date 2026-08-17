package com.momentum.domain.anchorpoint.service.typedecider;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.dto.RecentAnchorPoints;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.stock.Stock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAnchorPointTypeDecider {

  private static final BigDecimal FLAT_PRICE_THRESHOLD_PERCENT = BigDecimal.valueOf(1.0);
  private static final BigDecimal PERCENT_MULTIPLIER = BigDecimal.valueOf(100);
  private static final int RATIO_SCALE = 10;

  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final FlatPriceTypeResolver flatPriceTypeResolver;
  private final SlopedPriceTypeResolver slopedPriceTypeResolver;

  public List<StockAnchorPoint> resolvePointTypes(Stock stock) {
    RecentAnchorPoints points = stockAnchorPointRepository.findRecentAnchorPoints(stock.getId())
        .orElseThrow(() -> new IllegalArgumentException("AnchorPoint가 3개 미만입니다. stockId: " + stock.getId()));

    if (isFlatPrice(points.previous(), points.target())) {
      return flatPriceTypeResolver.resolve(points.oldest(), points.previous(), points.target(), points.latest());
    }
    return slopedPriceTypeResolver.resolve(points.previous(), points.target(), points.latest());
  }

  private boolean isFlatPrice(StockAnchorPoint first, StockAnchorPoint second) {
    if (first == null || second == null) {
      return false;
    }
    BigDecimal firstPrice = BigDecimal.valueOf(first.getPrice());
    BigDecimal secondPrice = BigDecimal.valueOf(second.getPrice());
    return firstPrice.subtract(secondPrice)
        .abs()
        .divide(secondPrice, RATIO_SCALE, RoundingMode.HALF_UP)
        .multiply(PERCENT_MULTIPLIER)
        .compareTo(FLAT_PRICE_THRESHOLD_PERCENT) <= 0;
  }
}
