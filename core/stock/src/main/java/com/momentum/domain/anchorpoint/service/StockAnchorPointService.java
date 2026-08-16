package com.momentum.domain.anchorpoint.service;

import com.momentum.domain.anchorpoint.StockAnchorPointCalculationRepository;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointCalculation;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockAnchorPointService {

  private final StockAnchorPointCalculationRepository anchorPointCalculationRepository;
  private final StockAnchorPointRepository stockAnchorPointRepository;
  private final SwingDoorChannelCalculator swingDoorChannelCalculator;

  @Transactional
  public StockAnchorPoint resolveAnchorPoint(StockDailyCandle dailyCandle) {
    Optional<StockAnchorPoint> lastAnchorPoint = stockAnchorPointRepository.findLastStockAnchorPoint(dailyCandle.getStock());
    if (lastAnchorPoint.isEmpty()) {
      return createAnchorPoint(dailyCandle);
    }

    Optional<StockAnchorPointCalculation> lastCalculation = anchorPointCalculationRepository.findLastCalculationHistory(
        dailyCandle.getStock());
    if (lastCalculation.isEmpty()) {
      saveCalculation(swingDoorChannelCalculator.openChannel(lastAnchorPoint.get(), dailyCandle), lastAnchorPoint.get(), dailyCandle);
      return null;
    }

    StockAnchorPointCalculation calculation = lastCalculation.get();
    TrendChannel channel = swingDoorChannelCalculator.narrowChannel(calculation, dailyCandle);
    if (!channel.isOutOfChannel()) {
      saveCalculation(channel, calculation.getStockAnchorPoint(), dailyCandle);
      return null;
    }

    StockAnchorPoint newAnchorPoint = confirmAnchorOf(calculation);
    saveCalculation(swingDoorChannelCalculator.openChannel(newAnchorPoint, dailyCandle), newAnchorPoint, dailyCandle);
    return newAnchorPoint;
  }

  private StockAnchorPoint confirmAnchorOf(StockAnchorPointCalculation lastCalculation) {
    StockAnchorPoint anchorPoint = StockAnchorPoint.create(lastCalculation.getCurrentPrice(),
        lastCalculation.getVolume(), lastCalculation.getTradeDate(),
        lastCalculation.getStockAnchorPoint().getStock());
    return stockAnchorPointRepository.save(anchorPoint);
  }

  private void saveCalculation(TrendChannel channel, StockAnchorPoint anchorPoint, StockDailyCandle dailyCandle) {
    StockAnchorPointCalculation pointCalculation = StockAnchorPointCalculation.create(dailyCandle.getClosePrice(),
        dailyCandle.getVolume(), dailyCandle.getTradeDate(), channel.slopeUpperMax(), channel.slopeLowerMin(),
        anchorPoint);
    anchorPointCalculationRepository.save(pointCalculation);
  }

  private StockAnchorPoint createAnchorPoint(StockDailyCandle dailyCandle) {
    StockAnchorPoint anchorPoint = StockAnchorPoint.create(dailyCandle.getClosePrice(), dailyCandle.getVolume(),
        dailyCandle.getTradeDate(), dailyCandle.getStock());
    return stockAnchorPointRepository.save(anchorPoint);
  }
}
