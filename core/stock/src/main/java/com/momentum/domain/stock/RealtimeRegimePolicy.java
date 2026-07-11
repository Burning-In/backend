package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;

import com.momentum.domain.base.entity.StockBase;
import org.springframework.stereotype.Component;

@Component
public class RealtimeRegimePolicy {

  public StockRegime decide(long currentPrice, StockBase currentStockBase, Stock stock,
      double breakOutThreshold, long lastPricePointPrice) {
    // # 하방이탈 : 현재가 < 지지선
    if (currentStockBase.getLowestSupportLine() != null
        && currentStockBase.getLowestSupportLine().getPrice() > currentPrice) {
      return DOWNSIDE_BREAK;
    }

    // # 돌파실패
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && lastPricePointPrice > currentPrice) {
      return BREAKOUT_FAILED;
    }

    // # 돌파성공 & 돌파준비
    long resistanceUpperBound = currentStockBase.getHighestResistanceLine()
        .getUpperBound(breakOutThreshold);
    if (currentStockBase.isVcp()) {
      if (currentPrice > resistanceUpperBound) {
        return BREAKOUT_SUCCESS;
      }
      return BREAKOUT_READY;
    }
    return UNKNOWN;
  }
}
