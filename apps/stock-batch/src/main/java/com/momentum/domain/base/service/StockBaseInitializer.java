package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseInitializer {

  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockAnchorPoint confirmedAnchorPoint) {
    if (confirmedAnchorPoint.isSameType(LOW)) {
      initializeBaseWithPairPoint(HIGH, confirmedAnchorPoint);
    }

    if (confirmedAnchorPoint.isSameType(HIGH)) {
      initializeBaseWithPairPoint(LOW, confirmedAnchorPoint);
    }
  }

  private void initializeBaseWithPairPoint(StockAnchorPointType pairedType, StockAnchorPoint confirmedAnchorPoint) {
    Optional<StockAnchorPoint> pairedPointOpt = stockAnchorPointRepository.findLastAnchorPointWithoutBase(
        confirmedAnchorPoint.getStock(), pairedType);
    if (pairedPointOpt.isEmpty()) {
      return;
    }
    StockAnchorPoint pairedPoint = pairedPointOpt.get();
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedAnchorPoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedAnchorPoint.getTradeDate());
    StockBase newBase = createBase(confirmedAnchorPoint, pairedPoint, baseAverageVolume);
    stockBaseRepository.save(newBase);
  }

  private StockBase createBase(StockAnchorPoint confirmedAnchorPoint, StockAnchorPoint pairedPoint,
      long baseAverageVolume) {
    if (confirmedAnchorPoint.isSameType(HIGH)) {
      return StockBase.init(confirmedAnchorPoint, pairedPoint, baseAverageVolume);
    }
    return StockBase.init(pairedPoint, confirmedAnchorPoint, baseAverageVolume);
  }
}
