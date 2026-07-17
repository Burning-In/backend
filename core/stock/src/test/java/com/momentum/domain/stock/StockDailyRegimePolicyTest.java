package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockDailyRegimePolicyTest {

  private static final double BREAKOUT_THRESHOLD = 5.0;
  private static final long BASE_AVERAGE_VOLUME = 100_000L;
  private static final long HIGH_VOLUME = 200_000L; // 베이스 평균 초과
  private static final long LOW_VOLUME = 50_000L;   // 베이스 평균 이하

  private final StockDailyRegimePolicy stockDailyRegimePolicy = new StockDailyRegimePolicy();

  @Test
  @DisplayName("종가가 지지선 하단(이탈임계)보다 낮으면 DOWNSIDE_BREAK")
  void downsideBreak() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    // 지지선 하단 = 10_000 * 0.95 = 9_500, 종가 9_000 < 9_500
    StockRegime result = stockDailyRegimePolicy.decide(9_000L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 9_500L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(DOWNSIDE_BREAK);
  }

  @Test
  @DisplayName("이탈 저점(직전 특이점)에서 반등 중이어도 지지선 하단 아래면 VCP여도 DOWNSIDE_BREAK")
  void downsideBreakEvenWhenReboundingAboveRecentLow() {
    Stock stock = uptrend(DOWNSIDE_BREAK);
    StockBase base = baseOf(stock, 12_000L, 10_000L);
    base.update(null, List.of(100L, 50L)); // VCP여도 이탈이 우선

    // 이탈 저점 9_000이 LOW로 확정된 직후 반등: 종가 9_200 > 직전특이점 9_000 이지만 하단 9_500 아래
    StockRegime result = stockDailyRegimePolicy.decide(9_200L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 9_000L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(DOWNSIDE_BREAK);
  }

  @Test
  @DisplayName("돌파성공 상태에서 종가가 직전 특이점보다 낮으면 BREAKOUT_FAILED")
  void breakoutFailed() {
    Stock stock = uptrend(BREAKOUT_SUCCESS);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    // 지지선 하단 = 9_500, 종가 11_000 > 9_500 (이탈 아님) & 직전 11_500 > 11_000
    StockRegime result = stockDailyRegimePolicy.decide(11_000L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 11_500L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(BREAKOUT_FAILED);
  }

  @Test
  @DisplayName("상승추세에서 종가가 저항선 상단을 넘고 직전 특이점보다 높고 거래량이 베이스 평균을 웃돌면 BREAKOUT_SUCCESS")
  void breakoutSuccess() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    // 저항선 상단 = 12_000 * 1.05 = 12_600, 종가 13_000 > 12_600 & 직전 12_500 < 13_000 & 거래량 200_000 > 100_000
    StockRegime result = stockDailyRegimePolicy.decide(13_000L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 12_500L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("저항선 상단을 넘어도 거래량이 베이스 평균 이하면 돌파 확정이 아니라 BREAKOUT_READY")
  void notBreakoutSuccessWhenVolumeBelowBaseAverage() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    // 가격 조건은 돌파 확정과 동일하지만 거래량 50_000 <= 100_000 → 확정 불가, 대기 조건은 충족
    StockRegime result = stockDailyRegimePolicy.decide(13_000L, LOW_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 12_500L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("상승추세이고 VCP면 BREAKOUT_READY")
  void breakoutReadyByVcp() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);
    base.update(null, List.of(100L, 50L)); // 변동성 축소 이력 → VCP

    StockRegime result = stockDailyRegimePolicy.decide(12_100L, LOW_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 12_050L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("VCP가 아니어도 종가가 저항선 하단(접근임계)을 넘고 직전 특이점보다 높으면 BREAKOUT_READY")
  void breakoutReadyByLineApproach() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    // 저항선 하단 = 12_000 * 0.95 = 11_400, 종가 12_000 은 11_400 초과 & 상단(12_600) 미만, 직전 11_900 < 12_000
    StockRegime result = stockDailyRegimePolicy.decide(12_000L, LOW_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 11_900L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("어떤 조건에도 해당하지 않으면 UNKNOWN (판단 보류)")
  void unknownWhenNoConditionMatched() {
    // 상승추세가 아니라 돌파성공/대기 모두 불가, 이탈/실패도 아님
    Stock stock = Stock.of("종목", "000020", BREAKOUT_READY, StockTrend.OTHER);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    StockRegime result = stockDailyRegimePolicy.decide(12_100L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, recentPoint(stock, 12_050L), BREAKOUT_THRESHOLD);

    assertThat(result).isEqualTo(UNKNOWN);
  }

  @Test
  @DisplayName("주식종목이 없으면 IllegalArgumentException")
  void throwsWhenStockNull() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    assertThatThrownBy(() -> stockDailyRegimePolicy.decide(12_000L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        null, base, recentPoint(stock, 11_900L), BREAKOUT_THRESHOLD))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재 베이스가 없으면 IllegalArgumentException")
  void throwsWhenBaseNull() {
    Stock stock = uptrend(BREAKOUT_READY);

    assertThatThrownBy(() -> stockDailyRegimePolicy.decide(12_000L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, null, recentPoint(stock, 11_900L), BREAKOUT_THRESHOLD))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("직전 특이점이 없으면 IllegalArgumentException")
  void throwsWhenRecentPricePointNull() {
    Stock stock = uptrend(BREAKOUT_READY);
    StockBase base = baseOf(stock, 12_000L, 10_000L);

    assertThatThrownBy(() -> stockDailyRegimePolicy.decide(12_000L, HIGH_VOLUME, BASE_AVERAGE_VOLUME,
        stock, base, null, BREAKOUT_THRESHOLD))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private Stock uptrend(StockRegime regime) {
    return Stock.of("종목", "000020", regime, StockTrend.UPTREND);
  }

  private StockPricePoint recentPoint(Stock stock, long price) {
    return new StockPricePoint(price, 100L, LocalDate.now(), StockPricePointType.HIGH, null, stock);
  }

  private StockBase baseOf(Stock stock, long resistancePrice, long supportPrice) {
    StockPricePoint high = new StockPricePoint(resistancePrice, 100_000L, LocalDate.now(),
        StockPricePointType.HIGH, null, stock);
    StockPricePoint low = new StockPricePoint(supportPrice, 100_000L, LocalDate.now(),
        StockPricePointType.LOW, null, stock);
    return StockBase.init(high, low, 100_000L);
  }
}
