package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseConfirmer {

  private final StockBaseRepository stockBaseRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockCandleRepository stockCandleRepository;

  public StockBase resolve(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase) {
    if (isLowPointAboveBase(confirmedAnchorPoint, currentBase)) {
      Optional<StockAnchorPoint> pairedHighPointOpt = stockAnchorPointRepository.findHighAnchorPoint(currentBase,
          currentBase.getResistanceUpperBound());
      if (pairedHighPointOpt.isEmpty()) {
        return null;
      }
      StockAnchorPoint pairedHighPoint = pairedHighPointOpt.get();
      long baseAverageVolume = calculateAverageVolume(confirmedAnchorPoint, pairedHighPoint);
      return stockBaseRepository.save(
          StockBase.upper(pairedHighPoint, confirmedAnchorPoint, currentBase.getStageLevel(), baseAverageVolume));
    }

    if (isHighPointBelowBase(confirmedAnchorPoint, currentBase)) {
      Optional<StockAnchorPoint> pairedLowPointOpt = stockAnchorPointRepository.findLowAnchorPoint(currentBase,
          currentBase.getSupportLowerBound());
      if (pairedLowPointOpt.isEmpty()) {
        return null;
      }
      StockAnchorPoint pairedLowPoint = pairedLowPointOpt.get();
      long baseAverageVolume = calculateAverageVolume(confirmedAnchorPoint, pairedLowPoint);
      return stockBaseRepository.save(
          StockBase.lower(confirmedAnchorPoint, pairedLowPoint, currentBase.getStageLevel(), baseAverageVolume));
    }

    return null;
  }

  private long calculateAverageVolume(StockAnchorPoint confirmedAnchorPoint, StockAnchorPoint pairedPoint) {
    return stockCandleRepository.averageVolume(
        confirmedAnchorPoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedAnchorPoint.getTradeDate());
  }

  private boolean isLowPointAboveBase(StockAnchorPoint point, StockBase base) {
    return point.isSameType(LOW) && base.isAbove(point);
  }

  private boolean isHighPointBelowBase(StockAnchorPoint point, StockBase base) {
    return point.isSameType(HIGH) && base.isBelow(point);
  }
}
