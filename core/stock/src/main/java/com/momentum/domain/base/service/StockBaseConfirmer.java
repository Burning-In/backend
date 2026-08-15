package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseConfirmer {

  private final StockBaseRepository stockBaseRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockCandleRepository stockCandleRepository;

  // 이거 너무 길긴함, 그리고 가독성도 그렇게 좋지가 않아서, 리펙토링 아이디어가 있어야되는데...
  // 그리고 null을 주는게 맞냐고 하면 그건 아니잖아.. 생각을 좀 해봐야 되는데..
  public StockBase resolve(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase, double baseBoundaryThreshold) {
    if (isLowPointAboveBase(confirmedAnchorPoint, currentBase, baseBoundaryThreshold)) {
      long resistanceUpperBound = currentBase.getResistanceUpperBound(baseBoundaryThreshold);
      StockAnchorPoint pairedHighPoint = stockAnchorPointRepository.findHighAnchorPoint(currentBase, resistanceUpperBound)
          .orElseThrow(IllegalArgumentException::new);
      long baseAverageVolume = calculateAverageVolume(confirmedAnchorPoint, pairedHighPoint);
      return stockBaseRepository.save(
          StockBase.upper(pairedHighPoint, confirmedAnchorPoint, currentBase.getStageLevel(), baseAverageVolume));
    }

    if (isHighPointBelowBase(confirmedAnchorPoint, currentBase, baseBoundaryThreshold)) {
      long supportLowerBound = currentBase.getSupportLowerBound(baseBoundaryThreshold);
      StockAnchorPoint pairedLowPoint = stockAnchorPointRepository.findLowAnchorPoint(currentBase, supportLowerBound)
          .orElseThrow(IllegalArgumentException::new);
      long baseAverageVolume = calculateAverageVolume(confirmedAnchorPoint, pairedLowPoint);
      return stockBaseRepository.save(
          StockBase.init(pairedLowPoint, confirmedAnchorPoint, baseAverageVolume));
    }

    return null;
  }

  private long calculateAverageVolume(StockAnchorPoint confirmedAnchorPoint, StockAnchorPoint pairedPoint) {
    return stockCandleRepository.averageVolume(
        confirmedAnchorPoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedAnchorPoint.getTradeDate());
  }

  private boolean isLowPointAboveBase(StockAnchorPoint point, StockBase base, double threshold) {
    return point.isSameType(LOW)
        && point.getPrice() >= base.getResistanceLowerBound(threshold);
  }

  private boolean isHighPointBelowBase(StockAnchorPoint point, StockBase base, double threshold) {
    return point.isSameType(HIGH)
        && point.getPrice() < base.getSupportUpperBound(threshold);
  }
}
