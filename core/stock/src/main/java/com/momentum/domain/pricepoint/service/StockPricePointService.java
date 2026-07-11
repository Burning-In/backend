package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.StockPricePointCalculationRepository;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockPricePointService {

  private final StockPricePointCalculationRepository pricePointCalculationRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final SwingDoorCalculator swingDoorCalculator;

  public StockPricePoint resolvePricePoint(StockDailyCandle dailyCandle) {
    Optional<StockPricePoint> lastPricePoint = stockPricePointRepository
        .findLastStockPricePoint(dailyCandle.getStock());
    if (lastPricePoint.isEmpty()) {
      return initializePricePoint(dailyCandle);
    }

    Optional<StockPricePointCalculation> lastCalculation = pricePointCalculationRepository
        .findLastCalculationHistory(dailyCandle.getStock());
    if (lastCalculation.isEmpty()) {
      swingDoorCalculator.initializeCalculation(lastPricePoint.get(), dailyCandle);
      return null;
    }

    return processSwingDoor(lastCalculation.get(), dailyCandle);
  }

  private StockPricePoint initializePricePoint(StockDailyCandle dailyCandle) {
    StockPricePoint pricePoint = StockPricePoint.init(dailyCandle.getClosePrice(), dailyCandle.getVolume(),
        dailyCandle.getTradeDate(), dailyCandle.getStock());
    return stockPricePointRepository.save(pricePoint);
  }

  private StockPricePoint processSwingDoor(StockPricePointCalculation lastCalculation,
      StockDailyCandle dailyCandle) {
    StockPricePoint newPricePoint = swingDoorCalculator.resolve(lastCalculation, dailyCandle);
    swingDoorCalculator.initializeCalculation(newPricePoint, dailyCandle);
    return newPricePoint;
  }
}
