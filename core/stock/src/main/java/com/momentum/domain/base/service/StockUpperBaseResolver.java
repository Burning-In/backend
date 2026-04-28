package com.momentum.domain.base.service;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockCandleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockUpperBaseResolver {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;

  // 상단 베이스 생성
  public StockBase resolve(StockPricePoint pivot, StockBase currentBase, double priceSimilarityThreshold,
      double baseBoundaryThreshold, long averageDailyVolume) {
    long resistanceLowerBound = calculateLowerPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold);

    // 저항선 -5% 이내 저점 → 새로운 베이스 생성
    if (pivot.getPrice() >= resistanceLowerBound) {
      StockPricePoint upperPricePoint = stockPricePointRepository.findUpperPricePoint(currentBase.getCreatedAt().toInstant(),
              calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold))
          .orElseThrow(() -> new IllegalArgumentException("해당 저점에 매칭되는 고점이 없습니다."));
      return StockBase.create(upperPricePoint, pivot,
          currentBase.getStageLevel() + 1, averageDailyVolume);
    }

    // 저항선 -5% 초과 && 지지선 +5% 이내 → 기존베이스 유지, 미소속 포인트 흡수
    long supportUpperBound = calculateUpperPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold);
    if (pivot.getPrice() < resistanceLowerBound && pivot.getPrice() >= supportUpperBound) {
      List<StockPricePoint> unassignedPoints = stockPricePointRepository
          .findUnassignedPointsSinceBase(currentBase.getCreatedAt().toInstant());
      currentBase.addPoints(unassignedPoints, averageDailyVolume, priceSimilarityThreshold);
      return currentBase;
    }

    // 지지선 -5% 미만 → stageLevel 조정
    if (pivot.getPrice() < calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold)) {
      StockBase lastBase = stockBaseRepository
          .findPreviousBase(pivot.getStock().getId(), currentBase.getCreatedAt().toInstant())
          .orElseThrow(() -> new IllegalArgumentException("이전 베이스가 없습니다."));
      if (pivot.getPrice() < calculateLowerPrice(lastBase.getStrongestResistanceLinePrice(), baseBoundaryThreshold)) {
        currentBase.update(lastBase.getStageLevel());
        return currentBase;
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
