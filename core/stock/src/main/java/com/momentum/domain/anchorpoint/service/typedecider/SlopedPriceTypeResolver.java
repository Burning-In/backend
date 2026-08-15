package com.momentum.domain.anchorpoint.service.typedecider;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlopedPriceTypeResolver {

  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockAnchorPointTypeClassifier stockAnchorPointTypeClassifier;

  public List<StockAnchorPoint> resolve(StockAnchorPoint previous, StockAnchorPoint target, StockAnchorPoint next) {
    StockAnchorPointType newType = stockAnchorPointTypeClassifier.classify(
        previous.getPrice(),
        target.getPrice(),
        next.getPrice()
    );
    target.updateType(newType);
    return stockAnchorPointRepository.saveAll(List.of(target));
  }
}
