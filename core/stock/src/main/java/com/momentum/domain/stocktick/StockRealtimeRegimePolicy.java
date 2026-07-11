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
@RequiredArgsConstructor
public class StockRealtimeRegimePolicy {

  private final StockPricePointRepository stockPricePointRepository;
  private final StockBaseRepository stockBaseRepository;

  public StockRegime determine(long currentPrice, Stock stock, double breakOutThreshold) {
    if (stock == null) {
      return UNKNOWN;
    }
    StockBase currentBase = stockBaseRepository.findCurrentBaseWithLines(stock)
        .orElseThrow(IllegalStateException::new);
    StockPricePoint lastPricePoint = stockPricePointRepository.findLastStockPricePoint(stock)
        .orElseThrow(IllegalStateException::new);

    // # 하방이탈 : 현재가 < 지지선 하단(임계 적용)
    if (currentBase.getLowestSupportLine() != null
        && currentBase.getLowestSupportLine().getLowerBound(breakOutThreshold) > currentPrice) {
      return DOWNSIDE_BREAK;
    }

    // # 돌파실패 : 마지막점 타입을 설정해야되는 이유
    if (stock.getStockRegime().equals(BREAKOUT_SUCCESS) && lastPricePoint.getPrice() > currentPrice) {
      return BREAKOUT_FAILED;
    }

    // # 돌파성공 & 돌파준비
    long resistanceUpperBound = currentBase.getHighestResistanceLine().getUpperBound(breakOutThreshold);
    if (!currentBase.isVcp()) {
      return UNKNOWN;
    }
    if (currentPrice > resistanceUpperBound) {
      return BREAKOUT_SUCCESS;
    }
    return BREAKOUT_READY;
  }
}
