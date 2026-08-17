package com.momentum.domain.stock;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_FAILED;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseLine;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import org.springframework.stereotype.Component;

@Component
public class StockDailyRegimePolicy {

  public StockRegime decide(long closePrice, long volume, long baseAverageVolume, Stock stock, StockBase currentBase,
      StockAnchorPoint lastAnchorPoint, double thresholdPercent) {
    validate(stock, currentBase, lastAnchorPoint);
    StockBaseLine highestResistanceLine = currentBase.getHighestResistanceLine();
    StockBaseLine lowestSupportLine = currentBase.getLowestSupportLine();

    if (closePrice < lowestSupportLine.getLowerBound(thresholdPercent)) {
      return DOWNSIDE_BREAK;
    }
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && closePrice < lastAnchorPoint.getPrice()) {
      return BREAKOUT_FAILED;
    }
    if (isBreakoutSuccess(stock, closePrice, volume, baseAverageVolume, highestResistanceLine, lastAnchorPoint,
        thresholdPercent)) {
      return BREAKOUT_SUCCESS;
    }
    if (isBreakoutReady(stock, closePrice, highestResistanceLine, lowestSupportLine, lastAnchorPoint, currentBase,
        thresholdPercent)) {
      return BREAKOUT_READY;
    }
    return UNKNOWN;
  }

  private void validate(Stock stock, StockBase currentBase, StockAnchorPoint recentAnchorPoint) {
    if (stock == null) {
      throw new IllegalArgumentException("stock is null");
    }
    if (currentBase == null) {
      throw new IllegalArgumentException("currentBase is null");
    }
    if (recentAnchorPoint == null) {
      throw new IllegalArgumentException("lastAnchorPoint is null");
    }
    if (currentBase.getHighestResistanceLine() == null || currentBase.getLowestSupportLine() == null) {
      throw new IllegalArgumentException("currentBase resistance/support line is null");
    }
  }

  private boolean isBreakoutSuccess(Stock stock, long closePrice, long volume, long baseAverageVolume,
      StockBaseLine highestResistanceLine, StockAnchorPoint lastAnchorPoint, double thresholdPercent) {
    return stock.getStockTrend().equals(StockTrend.UPTREND)
        && closePrice > highestResistanceLine.getUpperBound(thresholdPercent)
        && closePrice > lastAnchorPoint.getPrice()
        && volume > baseAverageVolume;
  }

  private boolean isBreakoutReady(Stock stock, long closePrice, StockBaseLine highestResistanceLine,
      StockBaseLine lowestSupportLine, StockAnchorPoint lastAnchorPoint, StockBase currentBase, double thresholdPercent) {
    return stock.getStockTrend().equals(StockTrend.UPTREND) && (currentBase.getVcp().isVcp()
        || (closePrice > highestResistanceLine.getLowerBound(thresholdPercent) && closePrice > lowestSupportLine.getPrice()
        && closePrice > lastAnchorPoint.getPrice()));
  }
}
