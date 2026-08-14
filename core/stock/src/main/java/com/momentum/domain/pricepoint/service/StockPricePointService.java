package com.momentum.domain.pricepoint.service;

import com.momentum.domain.pricepoint.StockPricePointCalculationRepository;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockPricePointService {

  private final StockPricePointCalculationRepository pricePointCalculationRepository;
  private final StockPricePointRepository stockPricePointRepository;
  private final SwingDoorChannelCalculator swingDoorChannelCalculator;

  @Transactional
  public StockPricePoint resolvePricePoint(StockDailyCandle dailyCandle) {
    Optional<StockPricePoint> lastPricePoint = stockPricePointRepository.findLastStockPricePoint(dailyCandle.getStock());
    if (lastPricePoint.isEmpty()) {
      return createPricePoint(dailyCandle);
    }

    Optional<StockPricePointCalculation> lastCalculation = pricePointCalculationRepository.findLastCalculationHistory(
        dailyCandle.getStock());
    if (lastCalculation.isEmpty()) {
      saveCalculation(swingDoorChannelCalculator.openChannel(lastPricePoint.get(), dailyCandle), lastPricePoint.get(), dailyCandle);
      return null;
    }

    StockPricePointCalculation calculation = lastCalculation.get();
    TrendChannel channel = swingDoorChannelCalculator.narrowChannel(calculation, dailyCandle);
    if (!channel.isOutOfChannel()) {
      saveCalculation(channel, calculation.getStockPricePoint(), dailyCandle);
      return null;
    }

    StockPricePoint newAnchorPoint = confirmAnchorOf(calculation);
    saveCalculation(swingDoorChannelCalculator.openChannel(newAnchorPoint, dailyCandle), newAnchorPoint, dailyCandle);
    return newAnchorPoint;
  }

  private StockPricePoint confirmAnchorOf(StockPricePointCalculation lastCalculation) {
    StockPricePoint anchorPoint = StockPricePoint.create(lastCalculation.getCurrentPrice(),
        lastCalculation.getVolume(), lastCalculation.getTradeDate(),
        lastCalculation.getStockPricePoint().getStock());
    return stockPricePointRepository.save(anchorPoint);
  }

  private void saveCalculation(TrendChannel channel, StockPricePoint anchorPoint, StockDailyCandle dailyCandle) {
    StockPricePointCalculation pointCalculation = StockPricePointCalculation.create(dailyCandle.getClosePrice(),
        dailyCandle.getVolume(), dailyCandle.getTradeDate(), channel.slopeUpperMax(), channel.slopeLowerMin(),
        anchorPoint);
    pricePointCalculationRepository.save(pointCalculation);
  }

  private StockPricePoint createPricePoint(StockDailyCandle dailyCandle) {
    StockPricePoint pricePoint = StockPricePoint.create(dailyCandle.getClosePrice(), dailyCandle.getVolume(),
        dailyCandle.getTradeDate(), dailyCandle.getStock());
    return stockPricePointRepository.save(pricePoint);
  }
}
