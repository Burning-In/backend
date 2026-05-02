package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseConfirmer {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;

  public StockBase resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double baseBoundaryThreshold,
      long averageDailyVolume) {
    long resistanceLowerBound = calculateLowerPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold);
    // 저항선 -5% 이내 저점 → 상단 새로운 베이스 생성
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      if (confirmedPricePoint.getPrice() >= resistanceLowerBound) {
        StockPricePoint upperPricePoint = stockPricePointRepository.findUpperPricePoint(currentBase.getCreatedAt().toInstant(),
                calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold))
            .orElseThrow(() -> new IllegalArgumentException("해당 저점에 매칭되는 고점이 없습니다."));
        StockBase newBase = StockBase.create(upperPricePoint, confirmedPricePoint, currentBase.getStageLevel() + 1,
            averageDailyVolume);
        return stockBaseRepository.save(newBase);
      }
    }

    // # 지지선 -5% 미만 고점 → 하단 새로운 베이스 생성
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      long supportLowerBound = calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold);
      if (confirmedPricePoint.getPrice() < supportLowerBound) {
        StockPricePoint lowerPricePoint = stockPricePointRepository
            .findLineLowerPricePoint(currentBase.getCreatedAt().toInstant(),
                calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold))
            .orElseThrow(() -> new IllegalArgumentException("해당 저점에 매칭되는 고점이 없습니다."));
        StockBase newBase = StockBase.init(lowerPricePoint, confirmedPricePoint, averageDailyVolume);
        return stockBaseRepository.save(newBase);
      }
    }

    return null;
  }

  private long calculateUpperPrice(long linePrice, double threshold) {
    return (long) (linePrice * (threshold / 100.0 + 1));
  }

  private long calculateLowerPrice(long linePrice, double threshold) {
    return (long) (linePrice * (-threshold / 100.0 + 1));
  }
}
