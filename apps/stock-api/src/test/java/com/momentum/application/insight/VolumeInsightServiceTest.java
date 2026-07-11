package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.VolumeResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class VolumeInsightServiceTest {

  @Autowired VolumeInsightService volumeInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired StockCandleRepository stockCandleRepository;

  private static final LocalDate TODAY = LocalDate.now();
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("당일 거래량이 평균보다 높으면 ratio 1 초과")
  void ratioExceedsOneWhenCurrentVolumeAboveAverage() {
    saveHistoricalCandles(30, 1000L);
    saveCandle(TODAY, 10000L);

    VolumeResponse result = volumeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.currentVolume()).isEqualTo(10000L);
    assertThat(result.volumeToBaselineRatio()).isGreaterThan(BigDecimal.ONE);
  }

  @Test
  @DisplayName("당일 거래량이 평균보다 낮으면 ratio 1 미만")
  void ratioBelowOneWhenCurrentVolumeUnderAverage() {
    saveHistoricalCandles(30, 10000L);
    saveCandle(TODAY, 1000L);

    VolumeResponse result = volumeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.currentVolume()).isEqualTo(1000L);
    assertThat(result.volumeToBaselineRatio()).isLessThan(BigDecimal.ONE);
  }

  @Test
  @DisplayName("모든 날 거래량이 같으면 ratio 1")
  void ratioIsOneWhenVolumeEqualsAverage() {
    saveHistoricalCandles(10, 5000L);
    saveCandle(TODAY, 5000L);

    VolumeResponse result = volumeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.volumeToBaselineRatio()).isEqualByComparingTo(BigDecimal.ONE);
  }

  private void saveHistoricalCandles(int days, long volume) {
    List<StockDailyCandle> candles = IntStream.rangeClosed(1, days)
        .mapToObj(i -> candle(TODAY.minusDays(i), volume))
        .toList();
    stockCandleRepository.saveAll(candles);
  }

  private void saveCandle(LocalDate date, long volume) {
    stockCandleRepository.save(candle(date, volume));
  }

  private StockDailyCandle candle(LocalDate date, long volume) {
    String rawDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
    return StockDailyCandle.create(stock, rawDate, 10000L, 10000L, 10000L, 10000L, volume);
  }
}
