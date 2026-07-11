package com.momentum.domain.base.service;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseConfirmer {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;

  // 이거 너무 길긴함, 그리고 가독성도 그렇게 좋지가 않아서, 리펙토링 아이디어가 있어야되는데...
  // 그리고 null을 주는게 맞냐고 하면 그건 아니잖아.. 생각을 좀 해봐야 되는데..
  public StockBase resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double baseBoundaryThreshold) {
    if (isLowPointAboveBase(confirmedPricePoint, currentBase, baseBoundaryThreshold)) {
      long resistanceUpperBound = currentBase.getResistanceUpperBound(baseBoundaryThreshold);
      StockPricePoint pairedHighPoint = stockPricePointRepository.findHighPricePoint(currentBase, resistanceUpperBound)
          .orElseThrow(IllegalArgumentException::new);
      long baseAverageVolume = calculateAverageVolume(confirmedPricePoint, pairedHighPoint);
      return stockBaseRepository.save(
          StockBase.upper(pairedHighPoint, confirmedPricePoint, currentBase.getStageLevel(), baseAverageVolume));
    }

    if (isHighPointBelowBase(confirmedPricePoint, currentBase, baseBoundaryThreshold)) {
      long supportLowerBound = currentBase.getSupportLowerBound(baseBoundaryThreshold);
      StockPricePoint pairedLowPoint = stockPricePointRepository.findLowPricePoint(currentBase, supportLowerBound)
          .orElseThrow(IllegalArgumentException::new);
      long baseAverageVolume = calculateAverageVolume(confirmedPricePoint, pairedLowPoint);
      return stockBaseRepository.save(
          StockBase.init(pairedLowPoint, confirmedPricePoint, baseAverageVolume));
    }

    return null;
  }

  private long calculateAverageVolume(StockPricePoint confirmedPricePoint, StockPricePoint pairedPoint) {
    return stockCandleRepository.averageVolume(
        confirmedPricePoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedPricePoint.getTradeDate());
  }

  private boolean isLowPointAboveBase(StockPricePoint point, StockBase base, double threshold) {
    return point.isSameType(LOW)
        && point.getPrice() >= base.getResistanceLowerBound(threshold);
  }

  private boolean isHighPointBelowBase(StockPricePoint point, StockBase base, double threshold) {
    return point.isSameType(HIGH)
        && point.getPrice() < base.getSupportUpperBound(threshold);
  }
}
