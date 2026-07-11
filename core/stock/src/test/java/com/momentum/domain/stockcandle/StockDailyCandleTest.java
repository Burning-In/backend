package com.momentum.domain.stockcandle;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockDailyCandleTest {

  @Test
  @DisplayName("create는 BASIC_ISO_DATE 문자열을 LocalDate로 파싱하고 가격/거래량을 담는다")
  void createParsesFields() {
    String tradeDateRaw = "20240115";
    LocalDate tradeDate = LocalDate.of(2024, 1, 15);
    long openPrice = 100L;
    long highPrice = 110L;
    long lowPrice = 90L;
    long closePrice = 105L;
    long volume = 1_000L;

    StockDailyCandle candle = StockDailyCandle.create(
        null, tradeDateRaw, openPrice, highPrice, lowPrice, closePrice, volume);

    assertSoftly(softly -> {
      softly.assertThat(candle.getTradeDate()).isEqualTo(tradeDate);
      softly.assertThat(candle.getOpenPrice()).isEqualTo(openPrice);
      softly.assertThat(candle.getHighPrice()).isEqualTo(highPrice);
      softly.assertThat(candle.getLowPrice()).isEqualTo(lowPrice);
      softly.assertThat(candle.getClosePrice()).isEqualTo(closePrice);
      softly.assertThat(candle.getVolume()).isEqualTo(volume);
    });
  }
}
