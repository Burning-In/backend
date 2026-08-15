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

  public void resolve(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase, double baseBoundaryThreshold) {
    Optional<StockBase> previousBaseOpt = stockBaseRepository
        .findPreviousBase(confirmedAnchorPoint.getStock(), currentBase.getCreatedAt().toInstant());
    if (previousBaseOpt.isEmpty()) {
      return;
    }
    // 애네도.. 그런데? 앞에 조건이 있음.. 근데 ㄷ안에 로직이 같아서 이거 그냥 앞에 가드로 넣으면 될 것 같은데?
    StockBase previousBase = previousBaseOpt.get();
    if (isLowPointDroppedToPreviousBase(confirmedAnchorPoint, currentBase, previousBase, baseBoundaryThreshold)) {
      currentBase.update(previousBase.getStageLevel(), null);
      stockBaseRepository.save(currentBase);
    }
    if (isHighPointRaisedToPreviousBase(confirmedAnchorPoint, currentBase, previousBase, baseBoundaryThreshold)) {
      currentBase.update(previousBase.getStageLevel(), null);
      stockBaseRepository.save(currentBase);
    }
  }

  private boolean isLowPointDroppedToPreviousBase(StockAnchorPoint point, StockBase currentBase,
      StockBase previousBase, double threshold) {
    return point.isSameType(LOW)
        && point.getPrice() < currentBase.getSupportLowerBound(threshold)
        && point.getPrice() < previousBase.getResistanceLowerBound(threshold);
  }

  private boolean isHighPointRaisedToPreviousBase(StockAnchorPoint point, StockBase currentBase,
      StockBase previousBase, double threshold) {
    return point.isSameType(HIGH)
        && point.getPrice() >= currentBase.getResistanceUpperBound(threshold)
        && point.getPrice() > previousBase.getSupportUpperBound(threshold);
  }
}
