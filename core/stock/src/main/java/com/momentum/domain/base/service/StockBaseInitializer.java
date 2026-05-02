package com.momentum.domain.base.service;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_LOW;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stockcandle.StockCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseInitializer {

  private final StockPricePointRepository stockPricePointRepository;
  private final StockBaseRepository stockBaseRepository;
  private final StockCandleRepository stockCandleRepository;

  public void resolve(StockPricePoint confirmedPricePoint) {
    if (confirmedPricePoint.isSameType(PIVOT_LOW)) {
      initializeBaseWithPairPoint(PIVOT_HIGH, confirmedPricePoint);
    }

    if (confirmedPricePoint.isSameType(PIVOT_HIGH)) {
      initializeBaseWithPairPoint(PIVOT_LOW, confirmedPricePoint);
    }
  }

  private void initializeBaseWithPairPoint(StockPricePointType pairedType, StockPricePoint confirmedPricePoint) {
    StockPricePoint pairedPoint = stockPricePointRepository.findLastPricePointWithoutBase(pairedType)
        .orElseThrow(IllegalArgumentException::new);
    long baseAverageVolume = stockCandleRepository.averageVolume(confirmedPricePoint.getStock(),
        pairedPoint.getTradeDate(),
        confirmedPricePoint.getTradeDate());
    StockBase newBase = StockBase.init(pairedPoint, confirmedPricePoint, baseAverageVolume);
    stockBaseRepository.save(newBase);
  }
}
