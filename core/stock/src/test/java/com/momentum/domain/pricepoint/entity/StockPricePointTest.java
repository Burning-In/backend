package com.momentum.domain.pricepoint.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockPricePointTest {

  private final Stock stock = Stock.of("종목", "000040", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("일봉으로 초기 특이점을 생성하면 종가/거래량/거래일을 그대로 가지고 타입은 INIT이다")
  void initFromCandle() {
    String tradeDateRaw = "20240115";
    LocalDate tradeDate = LocalDate.of(2024, 1, 15);
    long openPrice = 100L;
    long highPrice = 110L;
    long lowPrice = 90L;
    long closePrice = 105L;
    long candleVolume = 1_000L;
    String signUp = "2";
    StockDailyCandle candle = StockDailyCandle.create(
        stock, tradeDateRaw, openPrice, highPrice, lowPrice, closePrice, candleVolume, signUp);

    StockPricePoint point = StockPricePoint.init(
        candle.getClosePrice(), candle.getVolume(), candle.getTradeDate(), candle.getStock());

    assertSoftly(softly -> {
      softly.assertThat(point.getPrice()).isEqualTo(closePrice);
      softly.assertThat(point.getVolume()).isEqualTo(candleVolume);
      softly.assertThat(point.getTradeDate()).isEqualTo(tradeDate);
      softly.assertThat(point.getType()).isEqualTo(StockPricePointType.UNKNOWN);
    });
  }

  @Test
  @DisplayName("타입을 변경하고, 같은 타입이나 null이면 무시한다")
  void updateType() {
    long defaultPrice = 10_000L;
    StockPricePoint point = point(defaultPrice, StockPricePointType.UNKNOWN);

    point.updateType(StockPricePointType.HIGH);
    assertThat(point.getType()).isEqualTo(StockPricePointType.HIGH);

    point.updateType(null);
    assertThat(point.getType()).isEqualTo(StockPricePointType.HIGH);
  }

  @Test
  @DisplayName("base를 할당하고, 동일한 base면 그대로 둔다")
  void assignBase() {
    long defaultPrice = 10_000L;
    long resistancePrice = 12_000L;
    long supportPrice = 10_000L;
    StockPricePoint point = point(defaultPrice, StockPricePointType.HIGH);
    StockBase base = baseOf(resistancePrice, supportPrice);

    point.assignBase(base);

    assertThat(point.getStockBase()).isSameAs(base);
  }

  @Test
  @DisplayName("compareTo와 isSameType은 가격/타입 기준으로 동작한다")
  void compareToAndSameType() {
    long cheapPrice = 9_000L;
    long expensivePrice = 10_000L;
    StockPricePoint cheap = point(cheapPrice, StockPricePointType.LOW);
    StockPricePoint expensive = point(expensivePrice, StockPricePointType.HIGH);

    assertSoftly(softly -> {
      softly.assertThat(cheap.compareTo(expensive)).isNegative();
      softly.assertThat(cheap.isSameType(StockPricePointType.LOW)).isTrue();
      softly.assertThat(cheap.isSameType(StockPricePointType.HIGH)).isFalse();
    });
  }

  private StockPricePoint point(long price, StockPricePointType type) {
    return new StockPricePoint(price, 100L, LocalDate.now(), type, null, stock);
  }

  private StockBase baseOf(long resistancePrice, long supportPrice) {
    StockPricePoint high = point(resistancePrice, StockPricePointType.HIGH);
    StockPricePoint low = point(supportPrice, StockPricePointType.LOW);
    return StockBase.init(high, low, 100_000L);
  }
}
