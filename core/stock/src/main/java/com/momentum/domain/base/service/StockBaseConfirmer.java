package com.momentum.domain.base.service;

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

  public StockBase resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double baseBoundaryThreshold) {
    if (confirmedPricePoint.isAboveResistance(currentBase, baseBoundaryThreshold)) {
      long resistanceUpperBound = currentBase.getResistanceUpperBound(baseBoundaryThreshold);
      StockPricePoint pairedHighPoint = stockPricePointRepository.findHighPricePoint(currentBase, resistanceUpperBound)
          .orElseThrow(IllegalArgumentException::new);
      long baseAverageVolume = calculateAverageVolume(confirmedPricePoint, pairedHighPoint);
      return stockBaseRepository.save(
          StockBase.upper(pairedHighPoint, confirmedPricePoint, currentBase.getStageLevel(), baseAverageVolume));
    }

    if (confirmedPricePoint.isBelowSupport(currentBase, baseBoundaryThreshold)) {
      long supportLowerBound = currentBase.getSupportLowerBound(baseBoundaryThreshold);
      StockPricePoint pairedLowPoint = stockPricePointRepository.findLowPricePoint(currentBase, supportLowerBound)
          .orElseThrow(IllegalArgumentException::new);
      long baseAverageVolume = calculateAverageVolume(confirmedPricePoint, pairedLowPoint);
      return stockBaseRepository.save(
          StockBase.initOrLower(pairedLowPoint, confirmedPricePoint, baseAverageVolume));
    }

    return null;
  }

  private long calculateAverageVolume(StockPricePoint confirmedPricePoint, StockPricePoint pairedPoint) {
    return stockCandleRepository.averageVolume(
        confirmedPricePoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedPricePoint.getTradeDate());
  }
}
