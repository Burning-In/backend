package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.stockcandle.StockCandleRepository;
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
    StockAnchorPoint pairedPoint = stockAnchorPointRepository.findLastAnchorPointWithoutBase(pairedType)
        .orElseThrow(IllegalArgumentException::new);
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedAnchorPoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedAnchorPoint.getTradeDate());
    StockBase newBase = StockBase.init(pairedPoint, confirmedAnchorPoint, baseAverageVolume);
    stockBaseRepository.save(newBase);
  }
}
