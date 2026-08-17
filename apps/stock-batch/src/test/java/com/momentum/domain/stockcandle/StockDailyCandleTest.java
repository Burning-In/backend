package com.momentum.domain.stockcandle;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

  @ParameterizedTest(name = "rawDate=\"{0}\"")
  @DisplayName("create는 BASIC_ISO_DATE 형식이 아니거나 존재하지 않는 날짜면 IllegalArgumentException")
  @ValueSource(strings = {
      "2024-01-15",  // 구분자 포함 (BASIC_ISO_DATE는 yyyyMMdd만 허용)
      "20241315",    // 13월 (존재하지 않는 월)
      "20240230",    // 2월 30일 (존재하지 않는 일)
      "202401",      // 자릿수 부족
      "abcdefgh",    // 숫자가 아님
      ""             // 빈 문자열
  })
  void createThrowsWhenRawDateIsInvalid(String invalidRawDate) {
    assertThatThrownBy(() -> StockDailyCandle.create(
        null, invalidRawDate, 100L, 110L, 90L, 105L, 1_000L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("create는 rawDate가 null이면 IllegalArgumentException")
  void createThrowsWhenRawDateIsNull() {
    assertThatThrownBy(() -> StockDailyCandle.create(
        null, null, 100L, 110L, 90L, 105L, 1_000L))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
