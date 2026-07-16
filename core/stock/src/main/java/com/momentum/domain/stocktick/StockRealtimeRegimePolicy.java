package com.momentum.domain.stocktick;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class StockRealtimeRegimePolicy {

  public StockRegime determine(long currentPrice, Stock stock, StockBase currentBase, StockPricePoint lastPricePoint,
      double thresholdPercent) {
    validate(stock, currentBase, lastPricePoint);

    if (currentBase.getLowestSupportLine().getLowerBound(thresholdPercent) > currentPrice) {
      return DOWNSIDE_BREAK;
    }
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && lastPricePoint.getPrice() > currentPrice) {
      return BREAKOUT_FAILED;
    }
    if (currentBase.isVcp() && currentPrice > currentBase.getHighestResistanceLine().getUpperBound(thresholdPercent)) {
      return BREAKOUT_SUCCESS;
    }
    if (currentPrice > currentBase.getHighestResistanceLine().getUpperBound(thresholdPercent)) {
      return BREAKOUT_READY;
    }

    return UNKNOWN;
  }

  private void validate(Stock stock, StockBase currentBase, StockPricePoint lastPricePoint) {
    if (stock == null) {
      throw new IllegalArgumentException("stock is null");
    }
    if (currentBase == null) {
      throw new IllegalArgumentException("currentBase is null");
    }
    if (lastPricePoint == null) {
      throw new IllegalArgumentException("lastPricePoint is null");
    }
    if (currentBase.getHighestResistanceLine() == null || currentBase.getLowestSupportLine() == null) {
      throw new IllegalArgumentException("currentBase resistance/support line is null");
    }
  }
}
