package com.momentum.domain.base.service;

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
    if (currentBase.isFallingInBase(confirmedPricePoint, baseBoundaryThreshold)) {
      addUnsingedPointToCurrentBase(confirmedPricePoint, currentBase);
    }

    if (currentBase.isRaisedInBase(confirmedPricePoint, baseBoundaryThreshold)) {
      addUnsingedPointToCurrentBase(confirmedPricePoint, currentBase);
    }
  }

  private void addUnsingedPointToCurrentBase(StockPricePoint confirmedPricePoint, StockBase currentBase) {
    List<StockPricePoint> unassignedPoints = stockPricePointRepository.findUnassignedPointsSinceBase(currentBase);
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedPricePoint.getStock(),
        currentBase.getCreatedAt().toLocalDate(),
        confirmedPricePoint.getTradeDate());
    currentBase.addPoints(unassignedPoints, baseAverageVolume, PRICE_SIMILARITY_THRESHOLD);
    stockBaseRepository.save(currentBase);
  }
}
