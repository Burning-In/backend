package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBasePointIntegrator {

  public static final double PRICE_SIMILARITY_THRESHOLD = 2.0;

  private final StockBaseRepository stockBaseRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockCandleRepository stockCandleRepository;

  // 이거 같은거 아님?, 이거 앞에 그냥 가드 세워야 겠는데
  public void resolve(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase, double baseBoundaryThreshold) {
    if (isLowPointInsideBase(confirmedAnchorPoint, currentBase, baseBoundaryThreshold)) {
      addUnsingedPointToCurrentBase(confirmedAnchorPoint, currentBase);
    }

    if (isHighPointInsideBase(confirmedAnchorPoint, currentBase, baseBoundaryThreshold)) {
      addUnsingedPointToCurrentBase(confirmedAnchorPoint, currentBase);
    }
  }

  private boolean isLowPointInsideBase(StockAnchorPoint point, StockBase base, double threshold) {
    return point.isSameType(LOW)
        && point.getPrice() < base.getResistanceLowerBound(threshold)
        && point.getPrice() >= base.getSupportUpperBound(threshold);
  }

  private boolean isHighPointInsideBase(StockAnchorPoint point, StockBase base, double threshold) {
    return point.isSameType(HIGH)
        && point.getPrice() > base.getSupportUpperBound(threshold)
        && point.getPrice() < base.getResistanceUpperBound(threshold);
  }

  private void addUnsingedPointToCurrentBase(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase) {
    List<StockAnchorPoint> unassignedPoints = stockAnchorPointRepository.findUnassignedPointsSinceBase(currentBase);
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedAnchorPoint.getStock(),
        currentBase.getCreatedAt().toLocalDate(),
        confirmedAnchorPoint.getTradeDate());
    currentBase.integratePoints(unassignedPoints, baseAverageVolume, PRICE_SIMILARITY_THRESHOLD);
    stockBaseRepository.save(currentBase);
  }
}
