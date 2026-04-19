package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockPricePointRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBasePivotLowHandler {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockPricePoint pivot, StockBase currentBase, double priceSimilarityThreshold,
      double baseBoundaryThreshold) {
    long averageDailyVolume = stockCandleRepository.findAvgVolumeByStockAndDateAfter(
        pivot.getStock(),
        LocalDate.now().minusYears(1));
    long resistanceLowerBound = calculateLowerPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold);

    // 저항선 -5% 이내 저점 → 새로운 베이스 생성
    if (pivot.getPrice() >= resistanceLowerBound) {
      StockPricePoint upperPricePoint = stockPricePointRepository
          .findUpperPricePoint(currentBase.getCreatedAt().toInstant(),
              calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold))
          .orElseThrow(() -> new IllegalArgumentException("해당 저점에 매칭되는 고점이 없습니다."));
      stockBaseRepository.save(StockBase.create(upperPricePoint, pivot,
          currentBase.getStageLevel() + 1, averageDailyVolume));
      return;
    }

    // 저항선 -5% 초과 && 지지선 +5% 이내 → 미소속 포인트 흡수
    long supportUpperBound = calculateUpperPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold);
    if (pivot.getPrice() < resistanceLowerBound && pivot.getPrice() >= supportUpperBound) {
      List<StockPricePoint> unassignedPoints = stockPricePointRepository
          .findUnassignedPointsSinceBase(currentBase.getCreatedAt().toInstant());
      currentBase.addPoints(unassignedPoints, averageDailyVolume, priceSimilarityThreshold);
      stockBaseRepository.save(currentBase);
      return;
    }

    // 지지선 -5% 미만 → stageLevel 조정
    if (pivot.getPrice() < calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold)) {
      StockBase lastBase = stockBaseRepository
          .findPreviousBase(pivot.getStock().getId(), currentBase.getCreatedAt().toInstant())
          .orElseThrow(() -> new IllegalArgumentException("이전 베이스가 없습니다."));
      if (pivot.getPrice() < calculateLowerPrice(lastBase.getStrongestResistanceLinePrice(), baseBoundaryThreshold)) {
        currentBase.update(lastBase.getStageLevel());
        stockBaseRepository.save(currentBase);
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
