package com.momentum.domain.movingaverage;

import static com.momentum.domain.movingaverage.StockMovingAveragePeriod.MA_150;
import static com.momentum.domain.movingaverage.StockMovingAveragePeriod.MA_200;
import static com.momentum.domain.movingaverage.StockMovingAveragePeriod.MA_50;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockMovingAverageServiceTest {

  @Autowired
  private StockMovingAverageService stockMovingAverageService;

  @Autowired
  private StockCandleRepository stockCandleRepository;

  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  void 캔들이_200개면_MA50_MA150_MA200_모두_생성된다() {
    saveCandles(200, 1000L);

    List<StockMovingAverage> result = stockMovingAverageService.create(stock);

    assertThat(result).hasSize(3);
    assertThat(result).extracting(StockMovingAverage::getStockMovingAveragePeriod)
        .containsExactlyInAnyOrder(MA_50, MA_150, MA_200);
  }

  @Test
  void 캔들이_100개면_MA50만_생성된다() {
    saveCandles(100, 1000L);

    List<StockMovingAverage> result = stockMovingAverageService.create(stock);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStockMovingAveragePeriod()).isEqualTo(MA_50);
  }

  @Test
  void 캔들이_49개이하면_아무_MA도_생성되지_않는다() {
    saveCandles(49, 1000L);

    List<StockMovingAverage> result = stockMovingAverageService.create(stock);

    assertThat(result).isEmpty();
  }

  @Test
  void MA_값은_최근_N개_거래일_종가의_평균이다() {
    // 오래된 150개: 종가 1000
    saveRecentCandles(150, 201, 1000L);
    // 최근 50개: 종가 3000
    saveRecentCandles(50, 51, 3000L);

    List<StockMovingAverage> result = stockMovingAverageService.create(stock);

    // MA50  = 3000
    // MA150 = (50*3000 + 100*1000) / 150 = 1666
    // MA200 = (50*3000 + 150*1000) / 200 = 1500
    assertThat(findByPeriod(result, MA_50).getMa()).isEqualTo(3000L);
    assertThat(findByPeriod(result, MA_150).getMa()).isEqualTo(1666L);
    assertThat(findByPeriod(result, MA_200).getMa()).isEqualTo(1500L);
  }

  private void saveCandles(int count, long closePrice) {
    List<StockDailyCandle> candles = IntStream.rangeClosed(1, count)
        .mapToObj(i -> StockDailyCandle.create(
            stock,
            LocalDate.now().minusDays(count - i + 1).format(DateTimeFormatter.BASIC_ISO_DATE),
            closePrice, closePrice, closePrice, closePrice, 1000L))
        .toList();
    stockCandleRepository.saveAll(candles);
  }

  private void saveRecentCandles(int count, int startDaysAgo, long closePrice) {
    List<StockDailyCandle> candles = IntStream.rangeClosed(1, count)
        .mapToObj(i -> StockDailyCandle.create(
            stock,
            LocalDate.now().minusDays(startDaysAgo - i).format(DateTimeFormatter.BASIC_ISO_DATE),
            closePrice, closePrice, closePrice, closePrice, 1000L))
        .toList();
    stockCandleRepository.saveAll(candles);
  }

  private StockMovingAverage findByPeriod(List<StockMovingAverage> list, StockMovingAveragePeriod period) {
    return list.stream()
        .filter(ma -> ma.getStockMovingAveragePeriod() == period)
        .findFirst()
        .orElseThrow();
  }
}
