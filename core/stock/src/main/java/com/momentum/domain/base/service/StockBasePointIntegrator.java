package com.momentum.domain.base.service;


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

  public static final double PRICE_SIMILARITY_THRESHOLD_PERCENT = 2.0;

  private final StockBaseRepository stockBaseRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase) {
    if (!currentBase.contains(confirmedAnchorPoint)) {
      return;
    }
    addUnassignedPointsToCurrentBase(confirmedAnchorPoint, currentBase);
  }

  private void addUnassignedPointsToCurrentBase(StockAnchorPoint confirmedAnchorPoint, StockBase currentBase) {
    List<StockAnchorPoint> unassignedPoints = stockAnchorPointRepository.findUnassignedPointsSinceBase(currentBase);
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedAnchorPoint.getStock(),
        currentBase.getStartedAt(),
        confirmedAnchorPoint.getTradeDate());
    currentBase.integratePoints(unassignedPoints, baseAverageVolume, PRICE_SIMILARITY_THRESHOLD_PERCENT);
    stockBaseRepository.save(currentBase);
  }
}
