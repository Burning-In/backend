package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MomentumResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
class MomentumScoreInsightServiceTest {

  @Autowired MomentumInsightService momentumInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired StockCandleRepository stockCandleRepository;
  @Autowired StockRankScoreRepository stockRankScoreRepository;

  private static final LocalDate TODAY = LocalDate.now();
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("StockRankScore의 momentum을 퍼센트로 변환해 반환")
  void returnsYearlyChangeRateFromRankScore() {
    save253Candles(10000L, 12000L);
    saveRankScore(stock, new BigDecimal("0.2000"), TODAY); // 20% as decimal

    MomentumResponse result = momentumInsightService.query(stock.getCode(), TODAY);

    assertThat(result.currentPrice()).isEqualTo(12000L);
    assertThat(result.yearAgoPrice()).isEqualTo(10000L);
    assertThat(result.yearlyPriceChangeRate()).isEqualByComparingTo(new BigDecimal("20.0000"));
  }

  @Test
  @DisplayName("모멘텀이 하위권이면 낮은 퍼센타일 반환")
  void returnsLowPercentileWhenMomentumIsLow() {
    save253Candles(10000L, 10500L);
    saveRankScore(newStock("000040"), new BigDecimal("0.10"), TODAY);
    saveRankScore(newStock("000050"), new BigDecimal("0.20"), TODAY);
    saveRankScore(newStock("000070"), new BigDecimal("0.30"), TODAY);
    saveRankScore(stock, new BigDecimal("0.05"), TODAY);

    MomentumResponse result = momentumInsightService.query(stock.getCode(), TODAY);

    // 0.05보다 낮은 종목 = 0개 → 0/4 * 100 = 0.0%
    assertThat(result.percentileRank()).isEqualByComparingTo(new BigDecimal("0.0"));
  }

  @Test
  @DisplayName("모멘텀이 상위권이면 높은 퍼센타일 반환")
  void returnsHighPercentileWhenMomentumIsHigh() {
    save253Candles(10000L, 13000L);
    saveRankScore(newStock("000040"), new BigDecimal("0.05"), TODAY);
    saveRankScore(newStock("000050"), new BigDecimal("0.10"), TODAY);
    saveRankScore(newStock("000070"), new BigDecimal("0.20"), TODAY);
    saveRankScore(stock, new BigDecimal("0.30"), TODAY);

    MomentumResponse result = momentumInsightService.query(stock.getCode(), TODAY);

    // 0.30보다 낮은 종목 = 3개 → 3/4 * 100 = 75.0%
    assertThat(result.percentileRank()).isEqualByComparingTo(new BigDecimal("75.0"));
  }

  private void save253Candles(long yearAgoClose, long todayClose) {
    List<StockDailyCandle> candles = new ArrayList<>();
    candles.add(candle(TODAY.minusDays(252), yearAgoClose));
    IntStream.rangeClosed(1, 251).forEach(i ->
        candles.add(candle(TODAY.minusDays(252 - i), 11000L))
    );
    candles.add(candle(TODAY, todayClose));
    stockCandleRepository.saveAll(candles);
  }

  private StockDailyCandle candle(LocalDate date, long closePrice) {
    String rawDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
    return StockDailyCandle.create(stock, rawDate, closePrice, closePrice, closePrice, closePrice, 100000L);
  }

  private Stock newStock(String code) {
    return stockRepository.save(Stock.of("테스트종목", code, StockRegime.UNKNOWN, StockTrend.UPTREND));
  }

  private void saveRankScore(Stock s, BigDecimal momentum, LocalDate baseDate) {
    // momentum = (today - yearAgo) / yearAgo 가 되도록 종가 2개 구성
    long yearAgo = 100_000L;
    long today = momentum.add(BigDecimal.ONE).multiply(BigDecimal.valueOf(yearAgo)).longValue();
    stockRankScoreRepository.save(StockRankScore.create(List.of(today, yearAgo), baseDate, s));
  }
}
