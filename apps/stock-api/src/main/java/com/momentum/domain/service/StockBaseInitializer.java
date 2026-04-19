package com.momentum.domain.service;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPricePointType;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockPricePointRepository;
import java.time.LocalDate;
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

    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      StockPricePoint highPricePoint = stockPricePointRepository
          .findPricePointNoBase(StockPricePointType.PIVOT_HIGH)
          .orElseThrow(() -> new IllegalArgumentException("베이스가 없는 케이스에서 맞는 고점이 없습니다."));
      stockBaseRepository.save(StockBase.create(highPricePoint, confirmedPricePoint, 1, averageDailyVolume));
    }
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      StockPricePoint lowPricePoint = stockPricePointRepository
          .findPricePointNoBase(StockPricePointType.PIVOT_LOW)
          .orElseThrow(() -> new IllegalArgumentException("베이스가 없는 케이스에서 맞는 저점이 없습니다."));
      stockBaseRepository.save(StockBase.create(lowPricePoint, confirmedPricePoint, 1, averageDailyVolume));
    }
  }
}
