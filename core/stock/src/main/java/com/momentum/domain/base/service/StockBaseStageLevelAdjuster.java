package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseStageLevelAdjuster {

  private final StockBaseRepository stockBaseRepository;

  public void resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double baseBoundaryThreshold) {
    // 지지선 -5% 미만 → stageLevel 조정
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      if (confirmedPricePoint.getPrice() < calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold)) {
        StockBase lastBase = stockBaseRepository
            .findPreviousBase(confirmedPricePoint.getStock(), currentBase.getCreatedAt().toInstant())
            .orElseThrow(() -> new IllegalArgumentException("이전 베이스가 없습니다."));
        if (confirmedPricePoint.getPrice() < calculateLowerPrice(lastBase.getStrongestResistanceLinePrice(),
            baseBoundaryThreshold)) {
          currentBase.update(lastBase.getStageLevel());
          stockBaseRepository.save(currentBase);
        }
      }
    }

    // 저항선 +5% 이상 고점 → stageLevel 조정
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      if (confirmedPricePoint.getPrice() >= calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold)) {
        StockBase lastBase = stockBaseRepository
            .findPreviousBase(confirmedPricePoint.getStock(), currentBase.getCreatedAt().toInstant())
            .orElseThrow(() -> new IllegalArgumentException("이전 베이스가 없습니다."));
        if (confirmedPricePoint.getPrice() > calculateUpperPrice(lastBase.getStrongestSupportLinePrice(),
            baseBoundaryThreshold)) {
          currentBase.update(lastBase.getStageLevel());
          stockBaseRepository.save(currentBase);
        }
      }
    }
  }

  private long calculateUpperPrice(long linePrice, double threshold) {
    return (long) (linePrice * (threshold / 100.0 + 1));
  }

  private long calculateLowerPrice(long linePrice, double threshold) {
    return (long) (linePrice * (-threshold / 100.0 + 1));
  }
}
