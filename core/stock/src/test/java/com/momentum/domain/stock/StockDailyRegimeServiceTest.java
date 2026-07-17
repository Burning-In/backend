package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockDailyRegimeServiceTest {

  @Autowired
  private StockDailyRegimeService stockDailyRegimeService;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockBaseRepository stockBaseRepository;
  @Autowired
  private StockPricePointRepository stockPricePointRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;

  @Test
  @DisplayName("종가가 저항선 상단을 넘고 거래량이 베이스 평균을 웃돌면 BREAKOUT_SUCCESS로 갱신된다")
  void updatesToBreakoutSuccess() {
    Stock stock = stockRepository.save(Stock.of("삼성전자", "005930", BREAKOUT_READY, StockTrend.UPTREND));
    saveBase(stock, 10_000L, 8_000L);
    saveRecentPoint(stock, 9_500L);
    saveCandle(stock, 100L); // 베이스 기간 평균 거래량 ≈ 100

    // 저항선 상단 = 10_000 * 1.03 = 10_300, 종가 11_000 > 10_300 & 거래량 1_000 > 평균 100
    StockDailyCandle todayCandle = candle(stock, 11_000L, 1_000L);
    stockDailyRegimeService.resolveDailyRegime(stock, todayCandle);

    StockRegime saved = stockRepository.findByStockCode("005930").orElseThrow().getStockRegime();
    assertThat(saved).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("현재 베이스가 없으면 IllegalArgumentException")
  void throwsWhenBaseNotFound() {
    Stock stock = stockRepository.save(Stock.of("하이닉스", "000660", BREAKOUT_READY, StockTrend.UPTREND));

    assertThatThrownBy(() -> stockDailyRegimeService.resolveDailyRegime(stock, candle(stock, 11_000L, 1_000L)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private void saveBase(Stock stock, long resistancePrice, long supportPrice) {
    StockPricePoint high = new StockPricePoint(resistancePrice, 100_000L, LocalDate.now().minusDays(10),
        StockPricePointType.HIGH, null, stock);
    StockPricePoint low = new StockPricePoint(supportPrice, 100_000L, LocalDate.now().minusDays(20),
        StockPricePointType.LOW, null, stock);
    StockBase base = StockBase.init(high, low, 100_000L);
    base.update(null, List.of(100L, 50L));
    stockBaseRepository.save(base);
  }

  // findLatestByStock가 tradeDate 최신 1개를 반환하므로, 오늘 날짜로 저장해 직전 특이점으로 삼는다.
  private void saveRecentPoint(Stock stock, long price) {
    stockPricePointRepository.save(new StockPricePoint(price, 100L, LocalDate.now(),
        StockPricePointType.HIGH, null, stock));
  }

  // averageVolume은 [베이스 생성일(오늘), 당일] 범위의 캔들에서 계산되므로 오늘 날짜 캔들을 적재한다.
  private void saveCandle(Stock stock, long volume) {
    stockCandleRepository.save(candle(stock, 10_000L, volume));
  }

  private StockDailyCandle candle(Stock stock, long closePrice, long volume) {
    String rawDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    return StockDailyCandle.create(stock, rawDate, closePrice, closePrice, closePrice, closePrice, volume);
  }
}
