package com.momentum.domain.anchorpoint.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockAnchorPointTest {

  private final Stock stock = Stock.of("종목", "000040", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("일봉으로 초기 특이점을 생성하면 종가/거래량/거래일을 그대로 가지고 타입은 INIT이다")
  void createFromCandle() {
    String tradeDateRaw = "20240115";
    LocalDate tradeDate = LocalDate.of(2024, 1, 15);
    long openPrice = 100L;
    long highPrice = 110L;
    long lowPrice = 90L;
    long closePrice = 105L;
    long candleVolume = 1_000L;
    String signUp = "2";
    StockDailyCandle candle = StockDailyCandle.create(
        stock, tradeDateRaw, openPrice, highPrice, lowPrice, closePrice, candleVolume);

    StockAnchorPoint point = StockAnchorPoint.create(
        candle.getClosePrice(), candle.getVolume(), candle.getTradeDate(), candle.getStock());

    assertSoftly(softly -> {
      softly.assertThat(point.getPrice()).isEqualTo(closePrice);
      softly.assertThat(point.getVolume()).isEqualTo(candleVolume);
      softly.assertThat(point.getTradeDate()).isEqualTo(tradeDate);
      softly.assertThat(point.getType()).isEqualTo(StockAnchorPointType.UNKNOWN);
    });
  }

  @Test
  @DisplayName("타입을 변경하고, 같은 타입이나 null이면 무시한다")
  void updateType() {
    long defaultPrice = 10_000L;
    StockAnchorPoint point = point(defaultPrice, StockAnchorPointType.UNKNOWN);

    point.updateType(StockAnchorPointType.HIGH);
    assertThat(point.getType()).isEqualTo(StockAnchorPointType.HIGH);

    point.updateType(null);
    assertThat(point.getType()).isEqualTo(StockAnchorPointType.HIGH);
  }

  @Test
  @DisplayName("base를 할당하고, 동일한 base면 그대로 둔다")
  void assignBase() {
    long defaultPrice = 10_000L;
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    StockAnchorPoint point = point(defaultPrice, StockAnchorPointType.HIGH);
    StockBase base = baseOf(resistancePrice, supportPrice);

    point.assignBase(base);

    assertThat(point.getStockBase()).isSameAs(base);
  }

  @Test
  @DisplayName("compareTo와 isSameType은 가격/타입 기준으로 동작한다")
  void compareToAndSameType() {
    long cheapPrice = 9_000L;
    long expensivePrice = 10_000L;
    StockAnchorPoint cheap = point(cheapPrice, StockAnchorPointType.LOW);
    StockAnchorPoint expensive = point(expensivePrice, StockAnchorPointType.HIGH);

    assertSoftly(softly -> {
      softly.assertThat(cheap.compareTo(expensive)).isNegative();
      softly.assertThat(cheap.isSameType(StockAnchorPointType.LOW)).isTrue();
      softly.assertThat(cheap.isSameType(StockAnchorPointType.HIGH)).isFalse();
    });
  }

  private StockAnchorPoint point(long price, StockAnchorPointType type) {
    return new StockAnchorPoint(price, 100L, LocalDate.now(), type, null, stock);
  }

  private StockBase baseOf(long resistancePrice, long supportPrice) {
    StockAnchorPoint high = point(resistancePrice, StockAnchorPointType.HIGH);
    StockAnchorPoint low = point(supportPrice, StockAnchorPointType.LOW);
    return StockBase.init(high, low, 100_000L);
  }
}
