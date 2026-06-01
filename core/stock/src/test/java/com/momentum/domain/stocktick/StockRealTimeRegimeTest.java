package com.momentum.domain.stocktick;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;
import static com.momentum.domain.stock.StockRegime.decideRealTimeStockRegime;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockRealTimeRegimeTest {

  private static final double BREAKOUT_THRESHOLD = 5.0;

  @Test
  @DisplayName("현재가가 지지선보다 낮으면 DOWNSIDE_BREAK")
  void downsideBreak() {
    Stock stock = stockWithRegime(BREAKOUT_READY);
    StockBase stockBase = baseOf(stock, 12_000L, 10_000L);

    StockRegime result = decideRealTimeStockRegime(9_000L, stockBase, stock, BREAKOUT_THRESHOLD, 9_500L);

    assertThat(result).isEqualTo(DOWNSIDE_BREAK);
  }

  @Test
  @DisplayName("돌파성공 상태에서 현재가가 주가의 이전 특이점(고점/저점/flat)보다 낮으면 BREAKOUT_FAILED")
  void breakoutFailed() {
    Stock stock = stockWithRegime(BREAKOUT_SUCCESS);
    StockBase stockBase = baseOf(stock, 10_000L, 8_000L);

    StockRegime result = decideRealTimeStockRegime(9_000L, stockBase, stock, BREAKOUT_THRESHOLD, 9_500L);

    assertThat(result).isEqualTo(BREAKOUT_FAILED);
  }

  @Test
  @DisplayName("VCP이고 현재가가 저항선 상단(돌파임계)을 넘으면 BREAKOUT_SUCCESS")
  void breakoutSuccess() {
    Stock stock = stockWithRegime(BREAKOUT_READY);
    StockBase stockBase = baseOf(stock, 10_000L, 8_000L);
    markVcp(stockBase);

    // 저항선 상단 = 10_000 * 1.05 = 10_500, 현재가 11_000 > 10_500
    StockRegime result = decideRealTimeStockRegime(11_000L, stockBase, stock, BREAKOUT_THRESHOLD, 9_000L);

    assertThat(result).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("VCP이지만 현재가가 저항선 상단 이하면 BREAKOUT_READY")
  void breakoutReady() {
    Stock stock = stockWithRegime(BREAKOUT_READY);
    StockBase stockBase = baseOf(stock, 10_000L, 8_000L);
    markVcp(stockBase);

    // 저항선 상단 = 10_500, 현재가 10_000 <= 10_500
    StockRegime result = decideRealTimeStockRegime(10_000L, stockBase, stock, BREAKOUT_THRESHOLD, 9_000L);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("VCP가 아니면 UNKNOWN (판단 보류 — 기존 레짐 유지)")
  void unknownWhenNotVcp() {
    Stock stock = stockWithRegime(BREAKOUT_READY);
    StockBase stockBase = baseOf(stock, 10_000L, 8_000L);

    StockRegime result = decideRealTimeStockRegime(10_000L, stockBase, stock, BREAKOUT_THRESHOLD, 9_000L);

    assertThat(result).isEqualTo(UNKNOWN);
  }

  private Stock stockWithRegime(StockRegime regime) {
    return new Stock("테스트종목", "005930", regime, StockTrend.UPTREND);
  }

  private StockBase baseOf(Stock stock, long resistancePrice, long supportPrice) {
    StockPricePoint high = new StockPricePoint(resistancePrice, 100_000L, LocalDate.now(),
        StockPricePointType.PIVOT_HIGH, null, stock);
    StockPricePoint low = new StockPricePoint(supportPrice, 100_000L, LocalDate.now(),
        StockPricePointType.PIVOT_LOW, null, stock);
    return StockBase.initOrLower(high, low, 100_000L);
  }

  private void markVcp(StockBase stockBase) {
    // 변동성이 줄어드는(직전 > 직후) 이력 → isVcp = true
    stockBase.update(null, List.of(100L, 50L));
  }
}
