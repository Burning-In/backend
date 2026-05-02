package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBasePointIntegrator {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;

  public void resolve(StockPricePoint confirmedPricePoint, StockBase currentBase, double priceSimilarityThreshold,
      double baseBoundaryThreshold, long averageDailyVolume) {
    long resistanceLowerBound = calculateLowerPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold);
    long supportUpperBound = calculateUpperPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold);
    // 저항선 -5% 초과 && 지지선 +5% 이내 → 기존베이스 유지, 미소속 포인트 흡수
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      if (confirmedPricePoint.getPrice() < resistanceLowerBound && confirmedPricePoint.getPrice() >= supportUpperBound) {
        List<StockPricePoint> unassignedPoints = stockPricePointRepository
            .findUnassignedPointsSinceBase(currentBase.getCreatedAt().toInstant());
        currentBase.addPoints(unassignedPoints, averageDailyVolume, priceSimilarityThreshold);
        stockBaseRepository.save(currentBase);
      }
    }

    // 지지선 +5% 이상 && 저항선 +5% 미만 → 미소속 포인트 흡수
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      long resistanceUpperBound = calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold);
      if (confirmedPricePoint.getPrice() > supportUpperBound && confirmedPricePoint.getPrice() < resistanceUpperBound) {
        List<StockPricePoint> unassignedPoints = stockPricePointRepository
            .findUnassignedPointsSinceBase(currentBase.getCreatedAt().toInstant());
        currentBase.addPoints(unassignedPoints, averageDailyVolume, priceSimilarityThreshold);
        stockBaseRepository.save(currentBase);
      }
    }
  }

  // 해당로직 LinePrice안에 넣어야함
  private long calculateUpperPrice(long linePrice, double threshold) {
    return (long) (linePrice * (threshold / 100.0 + 1));
  }

  private long calculateLowerPrice(long linePrice, double threshold) {
    return (long) (linePrice * (-threshold / 100.0 + 1));
  }
}
