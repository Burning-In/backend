package com.momentum.domain.score;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockRankScoreServiceTest {

  private static final LocalDate BASE_DATE = LocalDate.of(2024, 1, 15);

  @Autowired
  private StockRankScoreService stockRankScoreService;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockRankScoreRepository stockRankScoreRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("종목", "000040", StockRegime.BREAKOUT_READY, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("종가가 252개 이상이면 종가로 모멘텀/FIP를 계산해 저장한다")
  void savesRankScoreWhenEnoughCandles() {
    // 최신 종가 12000, 252번째(가장 오래된) 종가 10000 → 모멘텀 (12000-10000)/10000 = 0.2
    saveCandles(252, 12_000L, 10_000L);

    stockRankScoreService.calculateDailyRankScores(stock, BASE_DATE);

    List<StockRankScore> saved = stockRankScoreRepository.findAllByBaseDate(BASE_DATE);
    assertThat(saved).hasSize(1);
    assertThat(saved.get(0).getMomentumScore().getValue()).isEqualByComparingTo(new BigDecimal("0.2"));
    assertThat(saved.get(0).getStock().getId()).isEqualTo(stock.getId());
  }

  @Test
  @DisplayName("종가가 252개 미만이면 저장하지 않는다")
  void doesNotSaveWhenNotEnoughCandles() {
    saveCandles(251, 12_000L, 10_000L);

    stockRankScoreService.calculateDailyRankScores(stock, BASE_DATE);

    assertThat(stockRankScoreRepository.findAllByBaseDate(BASE_DATE)).isEmpty();
  }

  // BASE_DATE(최신)부터 과거로 count개. 최신 종가 = newestClose, 가장 오래된 종가 = oldestClose
  private void saveCandles(int count, long newestClose, long oldestClose) {
    List<StockDailyCandle> candles = new ArrayList<>();
    candles.add(candle(BASE_DATE, newestClose));
    for (int i = 1; i < count - 1; i++) {
      candles.add(candle(BASE_DATE.minusDays(i), 11_000L));
    }
    candles.add(candle(BASE_DATE.minusDays(count - 1), oldestClose));
    stockCandleRepository.saveAll(candles);
  }

  private StockDailyCandle candle(LocalDate date, long closePrice) {
    String rawDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
    return StockDailyCandle.create(stock, rawDate, closePrice, closePrice, closePrice, closePrice, 100_000L, "2");
  }
}
