package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_FAILED;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.DIRECTION_UNDETERMINED;
import static com.momentum.domain.stock.StockRegime.DOWNSIDE_BREAK;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockRegimeTest {

  @Test
  @DisplayName("종가가 지지선보다 낮고 직전 특이점보다 낮으면 DOWNSIDE_BREAK")
  void downsideBreak() {
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    long closePrice = 9_000L;
    long recentPrice = 9_500L;
    double breakoutThreshold = 5.0;
    double lineApproachThreshold = 2.0;
    Stock stock = uptrend();
    StockBase base = baseOf(stock, resistancePrice, supportPrice);

    StockRegime result = new DailyRegimePolicy()
        .determine(stock, closePrice, recentPoint(stock, recentPrice), base, breakoutThreshold,
            lineApproachThreshold);

    assertThat(result).isEqualTo(DOWNSIDE_BREAK);
  }

  @Test
  @DisplayName("종가가 저항선보다 낮고 직전 특이점보다 낮으면(지지선 위) BREAKOUT_FAILED")
  void breakoutFailed() {
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    long closePrice = 11_000L;
    long recentPrice = 11_500L;
    double breakoutThreshold = 5.0;
    double lineApproachThreshold = 2.0;
    Stock stock = uptrend();
    StockBase base = baseOf(stock, resistancePrice, supportPrice);

    StockRegime result = new DailyRegimePolicy()
        .determine(stock, closePrice, recentPoint(stock, recentPrice), base, breakoutThreshold,
            lineApproachThreshold);

    assertThat(result).isEqualTo(BREAKOUT_FAILED);
  }

  @Test
  @DisplayName("상승추세에서 종가가 저항선을 돌파임계 이상 넘고 직전 특이점보다 높으면 BREAKOUT_SUCCESS")
  void breakoutSuccess() {
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    long closePrice = 13_000L;
    long recentPrice = 12_500L;
    double breakoutThreshold = 5.0;
    double lineApproachThreshold = 2.0;
    Stock stock = uptrend();
    StockBase base = baseOf(stock, resistancePrice, supportPrice);

    // gap = (13000-12000)/12000*100 = 8.3% > 5%
    StockRegime result = new DailyRegimePolicy()
        .determine(stock, closePrice, recentPoint(stock, recentPrice), base, breakoutThreshold,
            lineApproachThreshold);

    assertThat(result).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("상승추세이고 VCP면 BREAKOUT_READY")
  void breakoutReadyByVcp() {
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    long closePrice = 12_100L;
    long recentPrice = 12_050L;
    double breakoutThreshold = 5.0;
    double lineApproachThreshold = 2.0;
    List<Long> shrinkingVolatility = List.of(100L, 50L);
    Stock stock = uptrend();
    StockBase base = baseOf(stock, resistancePrice, supportPrice);
    base.update(null, shrinkingVolatility); // VCP 표시

    StockRegime result = new DailyRegimePolicy()
        .determine(stock, closePrice, recentPoint(stock, recentPrice), base, breakoutThreshold,
            lineApproachThreshold);

    assertThat(result).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("어떤 조건에도 해당하지 않으면 DIRECTION_UNDETERMINED")
  void directionUndetermined() {
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    long closePrice = 12_100L;
    long recentPrice = 12_050L;
    double breakoutThreshold = 5.0;
    double lineApproachThreshold = 2.0;
    Stock stock = Stock.of("종목", "000020", StockRegime.BREAKOUT_READY, StockTrend.OTHER);
    StockBase base = baseOf(stock, resistancePrice, supportPrice);

    StockRegime result = new DailyRegimePolicy()
        .determine(stock, closePrice, recentPoint(stock, recentPrice), base, breakoutThreshold,
            lineApproachThreshold);

    assertThat(result).isEqualTo(DIRECTION_UNDETERMINED);
  }

  private Stock uptrend() {
    return Stock.of("종목", "000020", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);
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
