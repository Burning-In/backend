package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseLine;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import org.springframework.stereotype.Component;

@Component
public class StockDailyRegimePolicy {

  public StockRegime decide(long closePrice, long volume, long baseAverageVolume, Stock stock, StockBase currentBase,
      StockPricePoint lastPricePoint, double thresholdPercent) {
    validate(stock, currentBase, lastPricePoint);
    StockBaseLine highestResistanceLine = currentBase.getHighestResistanceLine();
    StockBaseLine lowestSupportLine = currentBase.getLowestSupportLine();

    if (closePrice < lowestSupportLine.getLowerBound(thresholdPercent)) {
      return DOWNSIDE_BREAK;
    }
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && closePrice < lastPricePoint.getPrice()) {
      return BREAKOUT_FAILED;
    }
    if (isBreakoutSuccess(stock, closePrice, volume, baseAverageVolume, highestResistanceLine, lastPricePoint,
        thresholdPercent)) {
      return BREAKOUT_SUCCESS;
    }
    if (isBreakoutReady(stock, closePrice, highestResistanceLine, lowestSupportLine, lastPricePoint, currentBase,
        thresholdPercent)) {
      return BREAKOUT_READY;
    }
    return UNKNOWN;
  }

  private void validate(Stock stock, StockBase currentBase, StockPricePoint recentPricePoint) {
    if (stock == null) {
      throw new IllegalArgumentException("stock is null");
    }
    if (currentBase == null) {
      throw new IllegalArgumentException("currentBase is null");
    }
    if (recentPricePoint == null) {
      throw new IllegalArgumentException("lastPricePoint is null");
    }
    if (currentBase.getHighestResistanceLine() == null || currentBase.getLowestSupportLine() == null) {
      throw new IllegalArgumentException("currentBase resistance/support line is null");
    }
  }

  private boolean isBreakoutSuccess(Stock stock, long closePrice, long volume, long baseAverageVolume,
      StockBaseLine highestResistanceLine, StockPricePoint lastPricePoint, double thresholdPercent) {
    return stock.getStockTrend().equals(StockTrend.UPTREND)
        && closePrice > highestResistanceLine.getUpperBound(thresholdPercent)
        && closePrice > lastPricePoint.getPrice()
        && volume > baseAverageVolume;
  }

  private boolean isBreakoutReady(Stock stock, long closePrice, StockBaseLine highestResistanceLine,
      StockBaseLine lowestSupportLine, StockPricePoint lastPricePoint, StockBase currentBase, double thresholdPercent) {
    return stock.getStockTrend().equals(StockTrend.UPTREND) && (currentBase.getVcp().isVcp()
        || (closePrice > highestResistanceLine.getLowerBound(thresholdPercent) && closePrice > lowestSupportLine.getPrice()
        && closePrice > lastPricePoint.getPrice()));
  }
}
