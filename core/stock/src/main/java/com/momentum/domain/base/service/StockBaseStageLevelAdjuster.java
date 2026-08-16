package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseStageLevelAdjuster {

  private final StockBaseRepository stockBaseRepository;

  public void resolve(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase) {
    Optional<StockBase> previousBaseOpt = stockBaseRepository
        .findPreviousBase(confirmedAnchorPoint.getStock(), currentBase.getStartedAt());
    if (previousBaseOpt.isEmpty()) {
      return;
    }
    StockBase previousBase = previousBaseOpt.get();
    if (isLowPointDroppedToPreviousBase(confirmedAnchorPoint, currentBase, previousBase)) {
      currentBase.update(previousBase.getStageLevel(), null);
      stockBaseRepository.save(currentBase);
    }
    if (isHighPointRaisedToPreviousBase(confirmedAnchorPoint, currentBase, previousBase)) {
      currentBase.update(previousBase.getStageLevel(), null);
      stockBaseRepository.save(currentBase);
    }
  }

  private boolean isLowPointDroppedToPreviousBase(StockAnchorPoint point, StockBase currentBase,
      StockBase previousBase) {
    return point.isSameType(LOW)
        && currentBase.isBelow(point)
        && !previousBase.isAbove(point);
  }

  private boolean isHighPointRaisedToPreviousBase(StockAnchorPoint point, StockBase currentBase,
      StockBase previousBase) {
    return point.isSameType(HIGH)
        && currentBase.isAbove(point)
        && !previousBase.isBelow(point);
  }

}
