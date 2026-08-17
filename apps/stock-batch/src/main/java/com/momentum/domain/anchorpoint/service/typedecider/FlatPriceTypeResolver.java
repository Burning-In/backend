package com.momentum.domain.anchorpoint.service.typedecider;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlatPriceTypeResolver {

  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockAnchorPointTypeClassifier stockAnchorPointTypeClassifier;

  public List<StockAnchorPoint> resolve(StockAnchorPoint previous, StockAnchorPoint flatPoint, StockAnchorPoint target,
      StockAnchorPoint next) {
    StockAnchorPointType newType = stockAnchorPointTypeClassifier.classify(
        priceOrNull(previous),
        target.getPrice(),
        next.getPrice()
    );
    flatPoint.updateType(newType);
    target.updateType(newType);
    return stockAnchorPointRepository.saveAll(List.of(flatPoint, target));
  }

  private Long priceOrNull(StockAnchorPoint point) {
    if (point == null) {
      return null;
    }
    return point.getPrice();
  }
}
