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
public class StockBasePivotHighHandler {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockPricePoint pivot, StockBase currentBase, double priceSimilarityThreshold,
      double baseBoundaryThreshold) {
    long averageDailyVolume = stockCandleRepository.findAvgVolumeByStockAndDateAfter(
        pivot.getStock(),
        LocalDate.now().minusYears(1));
    long supportLowerBound = calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold);

    // 지지선 -5% 미만 고점 → 하단 새로운 베이스 생성
    if (pivot.getPrice() < supportLowerBound) {
      StockPricePoint lowerPricePoint = stockPricePointRepository
          .findLineLowerPricePoint(currentBase.getCreatedAt().toInstant(),
              calculateLowerPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold))
          .orElseThrow(() -> new IllegalArgumentException("해당 저점에 매칭되는 고점이 없습니다."));
      stockBaseRepository.save(StockBase.create(lowerPricePoint, pivot, 1, averageDailyVolume));
      return;
    }

    // 지지선 +5% 이상 && 저항선 +5% 미만 → 미소속 포인트 흡수
    long resistanceUpperBound = calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold);
    long supportUpperBound = calculateUpperPrice(currentBase.getLowestSupportLinePrice(), baseBoundaryThreshold);
    if (pivot.getPrice() > supportUpperBound && pivot.getPrice() < resistanceUpperBound) {
      List<StockPricePoint> unassignedPoints = stockPricePointRepository
          .findUnassignedPointsSinceBase(currentBase.getCreatedAt().toInstant());
      currentBase.addPoints(unassignedPoints, averageDailyVolume, priceSimilarityThreshold);
      stockBaseRepository.save(currentBase);
      return;
    }

    // 저항선 +5% 이상 고점 → stageLevel 조정
    if (pivot.getPrice() >= calculateUpperPrice(currentBase.getHighestResistancePrice(), baseBoundaryThreshold)) {
      StockBase lastBase = stockBaseRepository
          .findPreviousBase(pivot.getStock().getId(), currentBase.getCreatedAt().toInstant())
          .orElseThrow(() -> new IllegalArgumentException("이전 베이스가 없습니다."));
      if (pivot.getPrice() > calculateUpperPrice(lastBase.getStrongestSupportLinePrice(), baseBoundaryThreshold)) {
        currentBase.update(lastBase.getStageLevel());
        stockBaseRepository.save(currentBase);
      }
    }
  }

  // 아니 이거 암만 생각해도 미리 만들어서 던져주는게 좋을 것 같은데
  private long calculateUpperPrice(long linePrice, double threshold) {
    return (long) (linePrice * (threshold / 100.0 + 1));
  }

  private long calculateLowerPrice(long linePrice, double threshold) {
    return (long) (linePrice * (-threshold / 100.0 + 1));
  }
}
