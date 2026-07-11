package com.momentum.domain.base.service;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBasePointIntegrator {

  public static final double PRICE_SIMILARITY_THRESHOLD = 2.0;

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double baseBoundaryThreshold) {
    if (isLowPointInsideBase(confirmedPricePoint, currentBase, baseBoundaryThreshold)) {
      addUnsingedPointToCurrentBase(confirmedPricePoint, currentBase);
    }

    if (isHighPointInsideBase(confirmedPricePoint, currentBase, baseBoundaryThreshold)) {
      addUnsingedPointToCurrentBase(confirmedPricePoint, currentBase);
    }
  }

  private boolean isLowPointInsideBase(StockPricePoint point, StockBase base, double threshold) {
    return point.isSameType(LOW)
        && point.getPrice() < base.getResistanceLowerBound(threshold)
        && point.getPrice() >= base.getSupportUpperBound(threshold);
  }

  private boolean isHighPointInsideBase(StockPricePoint point, StockBase base, double threshold) {
    return point.isSameType(HIGH)
        && point.getPrice() > base.getSupportUpperBound(threshold)
        && point.getPrice() < base.getResistanceUpperBound(threshold);
  }

  private void addUnsingedPointToCurrentBase(StockPricePoint confirmedPricePoint, StockBase currentBase) {
    List<StockPricePoint> unassignedPoints = stockPricePointRepository.findUnassignedPointsSinceBase(currentBase);
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedPricePoint.getStock(),
        currentBase.getCreatedAt().toLocalDate(),
        confirmedPricePoint.getTradeDate());
    currentBase.integratePoints(unassignedPoints, baseAverageVolume, PRICE_SIMILARITY_THRESHOLD);
    stockBaseRepository.save(currentBase);
  }
}
