package com.momentum.domain.stocktick;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import org.springframework.stereotype.Component;

@Component
public class StockRealtimeRegimePolicy {

  public StockRegime decide(long currentPrice, Stock stock, StockBase currentBase, StockAnchorPoint lastAnchorPoint,
      double thresholdPercent) {
    validate(stock, currentBase, lastAnchorPoint);

    if (currentBase.getLowestSupportLine().getLowerBound(thresholdPercent) > currentPrice) {
      return DOWNSIDE_BREAK;
    }
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && lastAnchorPoint.getPrice() > currentPrice) {
      return BREAKOUT_FAILED;
    }
    if (stock.getStockTrend().equals(StockTrend.UPTREND) && currentBase.isVcp()
        && currentPrice > currentBase.getHighestResistanceLine().getUpperBound(thresholdPercent)) {
      return BREAKOUT_SUCCESS;
    }
    if (stock.getStockTrend().equals(StockTrend.UPTREND) && currentPrice > currentBase.getHighestResistanceLine().getUpperBound(thresholdPercent)) {
      return BREAKOUT_READY;
    }

    return UNKNOWN;
  }

  private void validate(Stock stock, StockBase currentBase, StockAnchorPoint lastAnchorPoint) {
    if (stock == null) {
      throw new IllegalArgumentException("stock is null");
    }
    if (currentBase == null) {
      throw new IllegalArgumentException("currentBase is null");
    }
    if (lastAnchorPoint == null) {
      throw new IllegalArgumentException("lastAnchorPoint is null");
    }
    if (currentBase.getHighestResistanceLine() == null || currentBase.getLowestSupportLine() == null) {
      throw new IllegalArgumentException("currentBase resistance/support line is null");
    }
  }
}
