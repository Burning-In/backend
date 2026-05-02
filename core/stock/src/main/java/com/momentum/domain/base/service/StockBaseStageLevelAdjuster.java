package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseStageLevelAdjuster {

  private final StockBaseRepository stockBaseRepository;

  public void resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double baseBoundaryThreshold) {
    Optional<StockBase> previousBaseOpt = stockBaseRepository
        .findPreviousBase(confirmedPricePoint.getStock(), currentBase.getCreatedAt().toInstant());
    if (previousBaseOpt.isEmpty()) {
      return;
    }
    StockBase previousBase = previousBaseOpt.get();
    if (confirmedPricePoint.isDroppedToPreviousBase(currentBase, previousBase, baseBoundaryThreshold)) {
      currentBase.update(previousBase.getStageLevel(), null);
      stockBaseRepository.save(currentBase);
    }
    if (confirmedPricePoint.isRaisedToPreviousBase(currentBase, previousBase, baseBoundaryThreshold)) {
      currentBase.update(previousBase.getStageLevel(), null);
      stockBaseRepository.save(currentBase);
    }
  }
}
