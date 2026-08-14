package com.momentum.domain.pricepoint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SwingDoorChannelCalculatorTest {

  private static final LocalDate ANCHOR_DATE = LocalDate.of(2026, 1, 5);
  private static final long ANCHOR_PRICE = 10_000L;

  private static final long DAY1_CLOSE = 11_000L;
  private static final long DAY2_CLOSE = 11_400L;
  private static final long DAY3_CLOSE = 11_700L;

  private static final String DAY1_UPPER = "700";
  private static final String DAY1_LOWER = "1300";
  private static final String DAY2_UPPER = "550";
  private static final String DAY2_LOWER = "850";
  private static final String DAY3_UPPER = "466.6666666667";
  private static final String DAY3_LOWER = "666.6666666667";

  private final SwingDoorChannelCalculator calculator = new SwingDoorChannelCalculator(
      new StockPricePointSlopeCalculator());

  private final Stock stock = Stock.of("테스트종목", "000500", StockRegime.UNKNOWN, StockTrend.OTHER);
  private final StockPricePoint anchorPoint = StockPricePoint.create(ANCHOR_PRICE, 1_000L, ANCHOR_DATE, stock);

  @Test
  @DisplayName("채널을 처음 열면 오늘 캔들의 기울기가 그대로 채널이 된다")
  void openChannel_returnsChannelOfSingleCandle() {
    // when
    TrendChannel channel = calculator.openChannel(anchorPoint, candleOf(1, DAY1_CLOSE));

    // then
    assertSoftly(softly -> {
      softly.assertThat(channel.slopeUpperMax()).isEqualByComparingTo(new BigDecimal(DAY1_UPPER));
      softly.assertThat(channel.slopeLowerMin()).isEqualByComparingTo(new BigDecimal(DAY1_LOWER));
      softly.assertThat(channel.isOutOfChannel()).isFalse();
    });
  }

  @Test
  @DisplayName("기준점에서 멀어질수록 같은 오차가 하루당 기울기로는 작아진다")
  void openChannel_withLaterCandle_dividesErrorByElapsedDays() {
    // when
    TrendChannel channel = calculator.openChannel(anchorPoint, candleOf(3, DAY3_CLOSE));

    // then
    assertSoftly(softly -> {
      softly.assertThat(channel.slopeUpperMax()).isEqualByComparingTo(new BigDecimal(DAY3_UPPER));
      softly.assertThat(channel.slopeLowerMin()).isEqualByComparingTo(new BigDecimal(DAY3_LOWER));
    });
  }

  @Test
  @DisplayName("오늘 기울기가 누적된 채널보다 느슨하면 누적값이 그대로 유지된다")
  void narrowChannel_withLooserSlope_keepsAccumulatedChannel() {
    // given
    StockPricePointCalculation lastCalculation = calculationOf(DAY1_UPPER, DAY1_LOWER);

    // when
    TrendChannel channel = calculator.narrowChannel(lastCalculation, candleOf(2, DAY2_CLOSE));

    // then
    assertSoftly(softly -> {
      softly.assertThat(channel.slopeUpperMax()).isEqualByComparingTo(new BigDecimal(DAY1_UPPER));
      softly.assertThat(channel.slopeLowerMin()).isEqualByComparingTo(new BigDecimal(DAY2_LOWER));
      softly.assertThat(channel.isOutOfChannel()).isFalse();
    });
  }

  @Test
  @DisplayName("1일차가 남긴 상한 덕분에 3일차에서 채널 이탈을 잡아낸다")
  void narrowChannel_withAccumulatedUpperBound_detectsOutOfChannel() {
    // given
    StockPricePointCalculation lastCalculation = calculationOf(DAY1_UPPER, DAY2_LOWER);

    // when
    TrendChannel channel = calculator.narrowChannel(lastCalculation, candleOf(3, DAY3_CLOSE));

    // then
    assertSoftly(softly -> {
      softly.assertThat(channel.slopeUpperMax()).isEqualByComparingTo(new BigDecimal(DAY1_UPPER));
      softly.assertThat(channel.slopeLowerMin()).isEqualByComparingTo(new BigDecimal(DAY3_LOWER));
      softly.assertThat(channel.isOutOfChannel()).isTrue();
    });
  }

  @Test
  @DisplayName("누적을 잃고 2일차 기울기만 들고 오면 같은 이탈을 놓친다")
  void narrowChannel_withoutAccumulation_missesOutOfChannel() {
    // given
    StockPricePointCalculation lastCalculation = calculationOf(DAY2_UPPER, DAY2_LOWER);

    // when
    TrendChannel channel = calculator.narrowChannel(lastCalculation, candleOf(3, DAY3_CLOSE));

    // then
    assertThat(channel.isOutOfChannel()).isFalse();
  }

  @Test
  @DisplayName("인자가 null이면 예외가 발생한다")
  void withNullArgument_throwsException() {
    assertSoftly(softly -> {
      softly.assertThatThrownBy(() -> calculator.openChannel(null, candleOf(1, DAY1_CLOSE)))
          .isInstanceOf(IllegalArgumentException.class);
      softly.assertThatThrownBy(() -> calculator.openChannel(anchorPoint, null))
          .isInstanceOf(IllegalArgumentException.class);
      softly.assertThatThrownBy(() -> calculator.narrowChannel(null, candleOf(1, DAY1_CLOSE)))
          .isInstanceOf(IllegalArgumentException.class);
      softly.assertThatThrownBy(() -> calculator.narrowChannel(calculationOf(DAY1_UPPER, DAY1_LOWER), null))
          .isInstanceOf(IllegalArgumentException.class);
    });
  }

  private StockDailyCandle candleOf(int daysAfterAnchor, long closePrice) {
    LocalDate tradeDate = ANCHOR_DATE.plusDays(daysAfterAnchor);
    return StockDailyCandle.create(stock, tradeDate.format(DateTimeFormatter.BASIC_ISO_DATE),
        closePrice, closePrice, closePrice, closePrice, 1_000L);
  }

  private StockPricePointCalculation calculationOf(String slopeUpperMax, String slopeLowerMin) {
    return StockPricePointCalculation.create(DAY2_CLOSE, 1_000L, ANCHOR_DATE.plusDays(2),
        new BigDecimal(slopeUpperMax), new BigDecimal(slopeLowerMin), anchorPoint);
  }
}
