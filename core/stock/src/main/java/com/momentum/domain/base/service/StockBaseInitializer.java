package com.momentum.domain.base.service;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseInitializer {

  private final StockPricePointRepository stockPricePointRepository;

  public StockBase resolve(StockPricePoint confirmedPricePoint, long averageDailyVolume) {
    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_LOW)) {
      StockPricePoint highPricePoint = stockPricePointRepository
          .findPricePointNoBase(StockPricePointType.PIVOT_HIGH)
          .orElseThrow(() -> new IllegalArgumentException("베이스가 없는 케이스에서 맞는 고점이 없습니다."));
      return StockBase.create(highPricePoint, confirmedPricePoint, 1, averageDailyVolume);
    }

    if (confirmedPricePoint.getStockPricePointType().equals(StockPricePointType.PIVOT_HIGH)) {
      StockPricePoint lowPricePoint = stockPricePointRepository
          .findPricePointNoBase(StockPricePointType.PIVOT_LOW)
          .orElseThrow(() -> new IllegalArgumentException("베이스가 없는 케이스에서 맞는 저점이 없습니다."));
      return StockBase.create(lowPricePoint, confirmedPricePoint, 1, averageDailyVolume);
    }

    return null;
  }
}
