package com.momentum.domain.service;

import com.momentum.domain.entity.analysis.base.StockBase;
import com.momentum.domain.entity.analysis.base.StockBaseLine;
import com.momentum.domain.entity.analysis.base.StockBaseLineType;
import com.momentum.domain.entity.analysis.pivot.StockPricePoint;
import com.momentum.domain.entity.analysis.pivot.StockPricePointType;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockPricePointRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseInitializer {

  private final StockBaseRepository stockBaseRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockPricePoint confirmedPricePoint) {
    long averageDailyVolume = stockCandleRepository.findAvgVolumeByStockAndDateAfter(
        confirmedPricePoint.getStock(),
        LocalDate.now().minusYears(1));

    Optional<StockBase> previousBaseOpt = stockBaseRepository.findCurrentBaseWithLines(
        confirmedPricePoint.getStock().getId());

    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      StockPricePoint highPricePoint = stockPricePointRepository
          .findPricePointNoBase(StockPricePointType.PIVOT_HIGH)
          .orElseThrow(() -> new IllegalArgumentException("베이스가 없는 케이스에서 맞는 고점이 없습니다."));
      StockBase newBase = StockBase.create(highPricePoint, confirmedPricePoint, 1, averageDailyVolume);
      convertLineTypes(previousBaseOpt, newBase);
      stockBaseRepository.save(newBase);
    }

    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      StockPricePoint lowPricePoint = stockPricePointRepository
          .findPricePointNoBase(StockPricePointType.PIVOT_LOW)
          .orElseThrow(() -> new IllegalArgumentException("베이스가 없는 케이스에서 맞는 저점이 없습니다."));
      StockBase newBase = StockBase.create(lowPricePoint, confirmedPricePoint, 1, averageDailyVolume);
      convertLineTypes(previousBaseOpt, newBase);
      stockBaseRepository.save(newBase);
    }
  }

  ///  추후 % 반영필요
  // 새 베이스 생성 시점에 이전 베이스의 라인 타입 전환
  // 저항선 → 지지선: 새 베이스의 지지선 가격 이하인 저항선 (저항선을 넘어 새 베이스가 형성됨)
  // 지지선 → 저항선: 새 베이스의 저항선 가격 이상인 지지선 (지지선이 무너져 새 저항선이 됨)
  private void convertLineTypes(Optional<StockBase> previousBaseOpt, StockBase newBase) {
    if (previousBaseOpt.isEmpty()) {
      return;
    }
    for (StockBaseLine line : previousBaseOpt.get().getStockBaseLines()) {
      if (line.getLineType() == StockBaseLineType.RESISTANCE
          && line.getPrice() <= newBase.getLowestSupportLinePrice()) {
        line.convertLineType();
      }
      if (line.getLineType() == StockBaseLineType.SUPPORT
          && line.getPrice() >= newBase.getHighestResistancePrice()) {
        line.convertLineType();
      }
    }
  }
}
