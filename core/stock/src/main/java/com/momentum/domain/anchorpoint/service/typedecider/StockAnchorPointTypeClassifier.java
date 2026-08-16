package com.momentum.domain.anchorpoint.service.typedecider;

import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import org.springframework.stereotype.Component;

@Component
public class StockAnchorPointTypeClassifier {

  public StockAnchorPointType classify(Long previousPrice, long targetPrice, long nextPrice) {
    if (previousPrice == null) {
      return classifyWithoutPrevious(targetPrice, nextPrice);
    }
    if (targetPrice > previousPrice && targetPrice > nextPrice) {
      return StockAnchorPointType.HIGH;
    }
    if (targetPrice < previousPrice && targetPrice < nextPrice) {
      return StockAnchorPointType.LOW;
    }
    if (targetPrice > previousPrice && targetPrice < nextPrice) {
      return StockAnchorPointType.ASCENDING;
    }
    if (targetPrice < previousPrice && targetPrice > nextPrice) {
      return StockAnchorPointType.DESCENDING;
    }
    return StockAnchorPointType.FLAT;
  }

  private StockAnchorPointType classifyWithoutPrevious(long targetPrice, long nextPrice) {
    if (targetPrice > nextPrice) {
      return StockAnchorPointType.HIGH;
    }
    if (targetPrice < nextPrice) {
      return StockAnchorPointType.LOW;
    }
    return StockAnchorPointType.FLAT;
  }
}
