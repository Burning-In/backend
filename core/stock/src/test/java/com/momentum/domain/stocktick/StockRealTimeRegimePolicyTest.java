package com.momentum.domain.stocktick;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockRealTimeRegimePolicyTest {

  private static final double THRESHOLD_PERCENT = 5.0;

  private final StockRealtimeRegimePolicy stockRealtimeRegimePolicy = new StockRealtimeRegimePolicy();

  @Test
  @DisplayName("주식종목이 없으면 IllegalArgumentException")
  void throwsWhenStockNull() {
    Stock stock = stock("000110", BREAKOUT_READY);
    StockBase base = base(stock, 10_000L, 8_000L, false);
    StockAnchorPoint lastPoint = point(stock, 9_000L);

    assertThatThrownBy(() -> stockRealtimeRegimePolicy.decide(10_000L, null, base, lastPoint, THRESHOLD_PERCENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재 베이스가 없으면 IllegalArgumentException")
  void throwsWhenBaseNull() {
    Stock stock = stock("000120", BREAKOUT_READY);
    StockAnchorPoint lastPoint = point(stock, 9_000L);

    assertThatThrownBy(() -> stockRealtimeRegimePolicy.decide(10_000L, stock, null, lastPoint, THRESHOLD_PERCENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("마지막 특이점이 없으면 IllegalArgumentException")
  void throwsWhenLastAnchorPointNull() {
    Stock stock = stock("000130", BREAKOUT_READY);
    StockBase base = base(stock, 10_000L, 8_000L, false);

    assertThatThrownBy(() -> stockRealtimeRegimePolicy.decide(10_000L, stock, base, null, THRESHOLD_PERCENT))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재가가 베이스의 지지선 하단(이탈임계)보다 낮으면 DOWNSIDE_BREAK")
  void downsideBreak() {
    Stock stock = stock("000040", BREAKOUT_READY);
    StockBase base = base(stock, 12_000L, 10_000L, false);
    StockAnchorPoint lastPoint = point(stock, 9_500L);

    // 지지선 하단 = 10_000 * 0.95 = 9_500, 현재가 9_000 < 9_500
    StockRegime result = stockRealtimeRegimePolicy.decide(9_000L, stock, base, lastPoint, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(DOWNSIDE_BREAK);
  }

  @Test
  @DisplayName("돌파성공 상태에서 현재가가 직전 특이점보다 낮으면 BREAKOUT_FAILED")
  void breakoutFailed() {
    Stock stock = stock("000050", BREAKOUT_SUCCESS);
    StockBase base = base(stock, 10_000L, 8_000L, false);
    StockAnchorPoint lastPoint = point(stock, 9_500L);

    // 지지선 하단 = 7_600, 현재가 9_000 > 7_600 (이탈 아님) & 직전 9_500 > 9_000
    StockRegime result = stockRealtimeRegimePolicy.decide(9_000L, stock, base, lastPoint, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(BREAKOUT_FAILED);
  }

  @Test
  @DisplayName("VCP이고 현재가가 저항선 상단(돌파임계)을 넘으면 BREAKOUT_SUCCESS")
  void breakoutSuccess() {
    Stock stock = stock("000070", BREAKOUT_READY);
    StockBase base = base(stock, 10_000L, 8_000L, true);
    StockAnchorPoint lastPoint = point(stock, 9_000L);

    // 저항선 상단 = 10_000 * 1.05 = 10_500, 현재가 11_000 > 10_500 & VCP
    StockRegime result = stockRealtimeRegimePolicy.decide(11_000L, stock, base, lastPoint, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("실시간에서만 현재가가 저항선 상단을 넘어도 VCP가 아니면 BREAKOUT_READY")
  void breakoutReady() {
    Stock stock = stock("000080", BREAKOUT_READY);
    StockBase base = base(stock, 10_000L, 8_000L, false);
    StockAnchorPoint lastPoint = point(stock, 9_000L);

    // 저항선 상단 = 10_500, 현재가 11_000 > 10_500 이지만 VCP 아님
    StockRegime result = stockRealtimeRegimePolicy.decide(11_000L, stock, base, lastPoint, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("돌파/이탈 어느 조건에도 해당하지 않으면 UNKNOWN (판단 보류 — 기존 레짐 유지)")
  void unknownWhenNoConditionMatched() {
    Stock stock = stock("000100", BREAKOUT_READY);
    StockBase base = base(stock, 10_000L, 8_000L, false);
    StockAnchorPoint lastPoint = point(stock, 9_000L);

    // 저항선 상단 = 10_500, 지지선 하단 = 7_600, 현재가 10_000 은 밴드 안
    StockRegime result = stockRealtimeRegimePolicy.decide(10_000L, stock, base, lastPoint, THRESHOLD_PERCENT);

    assertThat(result).isEqualTo(UNKNOWN);
  }

  private Stock stock(String code, StockRegime regime) {
    return Stock.of("테스트종목", code, regime, StockTrend.UPTREND);
  }

  private StockBase base(Stock stock, long resistancePrice, long supportPrice, boolean vcp) {
    StockAnchorPoint high = new StockAnchorPoint(resistancePrice, 100_000L, LocalDate.now().minusDays(10),
        StockAnchorPointType.HIGH, null, stock);
    StockAnchorPoint low = new StockAnchorPoint(supportPrice, 100_000L, LocalDate.now().minusDays(20),
        StockAnchorPointType.LOW, null, stock);
    StockBase base = StockBase.init(high, low, 100_000L);
    if (vcp) {
      // 변동성이 줄어드는(직전 > 직후) 이력 → isVcp = true
      base.update(null, List.of(100L, 50L));
    }
    return base;
  }

  private StockAnchorPoint point(Stock stock, long price) {
    return new StockAnchorPoint(price, 100_000L, LocalDate.now().minusDays(1),
        StockAnchorPointType.HIGH, null, stock);
  }
}
