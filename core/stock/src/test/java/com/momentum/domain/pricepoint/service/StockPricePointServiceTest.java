package com.momentum.domain.pricepoint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.pricepoint.StockPricePointCalculationRepository;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockPricePointServiceTest {

  @Autowired
  private StockPricePointService stockPricePointService;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockPricePointRepository stockPricePointRepository;
  @Autowired
  private StockPricePointCalculationRepository stockPricePointCalculationRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.OTHER));
  }

  @Test
  @DisplayName("기준점이 하나도 없으면 오늘 캔들이 첫 기준점으로 확정된다")
  void resolvePricePoint_withoutAnyAnchor_confirmsTodayAsFirstAnchor() {
    // when
    StockPricePoint confirmed = resolve("20240101", 10_500L, 1_000L);

    // then
    assertThat(confirmed).isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(confirmed.getPrice()).isEqualTo(10_500L);
      softly.assertThat(confirmed.getVolume()).isEqualTo(1_000L);
      softly.assertThat(confirmed.getTradeDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    });
  }

  @Test
  @DisplayName("첫 기준점이 확정된 날에는 기울기를 잴 다음 캔들이 없으므로 채널이 열리지 않는다")
  void resolvePricePoint_withoutAnyAnchor_doesNotOpenChannel() {
    // when
    resolve("20240101", 10_500L, 1_000L);

    // then
    assertThat(stockPricePointCalculationRepository.findLastCalculationHistory(stock)).isEmpty();
  }

  @Test
  @DisplayName("기준점만 있고 채널이 없으면 채널만 열고 기준점은 그대로 둔다")
  void resolvePricePoint_withAnchorButNoChannel_opensChannelKeepingAnchor() {
    // given
    StockPricePoint anchor = resolve("20240101", 10_000L, 1_000L);

    // when
    StockPricePoint confirmed = resolve("20240102", 10_500L, 1_200L);

    // then
    StockPricePointCalculation opened = lastCalculation();
    assertSoftly(softly -> {
      softly.assertThat(confirmed).isNull();
      softly.assertThat(lastAnchor().getId()).isEqualTo(anchor.getId());
      softly.assertThat(opened.getStockPricePoint().getId()).isEqualTo(anchor.getId());
      softly.assertThat(opened.getCurrentPrice()).isEqualTo(10_500L);
      softly.assertThat(channelOf(opened).isOutOfChannel()).isFalse();
    });
  }

  @Test
  @DisplayName("채널 안에 들어오는 캔들은 채널을 좁히기만 하고 기준점은 그대로 둔다")
  void resolvePricePoint_withinChannel_narrowsChannelKeepingAnchor() {
    // given
    StockPricePoint anchor = resolve("20240101", 10_000L, 1_000L);
    resolve("20240102", 10_500L, 1_200L);
    TrendChannel opened = channelOf(lastCalculation());

    // when
    StockPricePoint confirmed = resolve("20240103", 10_600L, 1_100L);

    // then
    TrendChannel narrowed = channelOf(lastCalculation());
    assertSoftly(softly -> {
      softly.assertThat(confirmed).isNull();
      softly.assertThat(lastAnchor().getId()).isEqualTo(anchor.getId());
      softly.assertThat(narrowed.slopeUpperMax()).isGreaterThanOrEqualTo(opened.slopeUpperMax());
      softly.assertThat(narrowed.slopeLowerMin()).isLessThanOrEqualTo(opened.slopeLowerMin());
      softly.assertThat(narrowed.isOutOfChannel()).isFalse();
    });
  }

  @Test
  @DisplayName("채널을 이탈하면 오늘이 아니라 채널 안에 있던 마지막 캔들이 새 기준점이 된다")
  void resolvePricePoint_outOfChannel_confirmsLastCandleInsideChannelAsAnchor() {
    // given
    resolve("20240101", 10_000L, 1_000L);
    resolve("20240102", 10_300L, 1_200L);
    resolve("20240103", 10_500L, 1_300L);

    // when
    StockPricePoint confirmed = resolve("20240104", 14_000L, 5_000L);

    // then
    assertThat(confirmed).isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(confirmed.getPrice()).isEqualTo(10_500L);
      softly.assertThat(confirmed.getVolume()).isEqualTo(1_300L);
      softly.assertThat(confirmed.getTradeDate()).isEqualTo(LocalDate.of(2024, 1, 3));
      softly.assertThat(lastAnchor().getId()).isEqualTo(confirmed.getId());
    });
  }

  @Test
  @DisplayName("채널을 이탈하면 새 기준점에서 오늘 캔들로 채널이 다시 열린다")
  void resolvePricePoint_outOfChannel_reopensChannelFromNewAnchor() {
    // given
    resolve("20240101", 10_000L, 1_000L);
    resolve("20240102", 10_300L, 1_200L);
    resolve("20240103", 10_500L, 1_300L);

    // when
    StockPricePoint confirmed = resolve("20240104", 14_000L, 5_000L);

    // then
    assertThat(confirmed).isNotNull();
    StockPricePointCalculation reopened = lastCalculation();
    assertSoftly(softly -> {
      softly.assertThat(reopened.getStockPricePoint().getId()).isEqualTo(confirmed.getId());
      softly.assertThat(reopened.getCurrentPrice()).isEqualTo(14_000L);
      softly.assertThat(reopened.getTradeDate()).isEqualTo(LocalDate.of(2024, 1, 4));
      softly.assertThat(channelOf(reopened).isOutOfChannel()).isFalse();
    });
  }

  private StockPricePoint resolve(String tradeDate, long closePrice, long volume) {
    StockDailyCandle candle = stockCandleRepository.save(
        StockDailyCandle.create(stock, tradeDate, closePrice, closePrice, closePrice, closePrice, volume));
    return stockPricePointService.resolvePricePoint(candle);
  }

  private StockPricePoint lastAnchor() {
    return stockPricePointRepository.findLastStockPricePoint(stock).orElseThrow();
  }

  private StockPricePointCalculation lastCalculation() {
    return stockPricePointCalculationRepository.findLastCalculationHistory(stock).orElseThrow();
  }

  private TrendChannel channelOf(StockPricePointCalculation calculation) {
    return new TrendChannel(calculation.getSlopeUpperMax(), calculation.getSlopeLowerMin());
  }
}
