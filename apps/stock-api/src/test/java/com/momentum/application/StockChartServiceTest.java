package com.momentum.application;

import static com.momentum.domain.ma.StockMovingAveragePeriod.MA_50;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.PIVOT_LOW;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.BaseListResponse.BaseItem;
import com.momentum.interfaces.api.stock.StockChartV1Dto.DailyCandleResponse;
import com.momentum.interfaces.api.stock.StockChartV1Dto.MovingAverageResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.IntToLongFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockChartServiceTest {

  @Autowired
  private StockChartService stockChartService;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockBaseRepository stockBaseRepository;

  @Test
  @DisplayName("일봉은 요청 날짜 범위만 오름차순으로 반환한다")
  void getDailyCandlesReturnsRange() {
    Stock stock = saveStock("000012");
    saveCandle(stock, LocalDate.of(2026, 5, 1), 100L);
    saveCandle(stock, LocalDate.of(2026, 5, 2), 200L);
    saveCandle(stock, LocalDate.of(2026, 5, 3), 300L);

    DailyCandleResponse response =
        stockChartService.getDailyCandles(stock.getCode(), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3));

    assertThat(response.candles()).hasSize(2);
    assertThat(response.candles().get(0).closePrice()).isEqualTo(200L);
    assertThat(response.candles().get(1).closePrice()).isEqualTo(300L);
  }

  @Test
  @DisplayName("이평선은 최근 N거래일 종가 평균을 롤링으로 계산한다")
  void getMovingAveragesComputesRollingSma() {
    Stock stock = saveStock("000010");
    LocalDate start = LocalDate.of(2026, 1, 1);
    saveCandles(stock, start, 50, i -> 1_000L);          // index 0~49 종가 1000
    saveCandle(stock, start.plusDays(50), 2_000L);        // index 50 종가 2000

    MovingAverageResponse response = stockChartService.getMovingAverages(
        stock.getCode(), MA_50, start.plusDays(49), start.plusDays(50));

    assertThat(response.period()).isEqualTo(MA_50);
    assertThat(response.dataPoints()).hasSize(2);
    assertThat(response.dataPoints().get(0).price()).isEqualTo(1_000L);   // 50개 평균
    assertThat(response.dataPoints().get(1).price()).isEqualTo(1_020L);   // (49*1000+2000)/50
  }

  @Test
  @DisplayName("N거래일치 데이터가 모이지 않으면 이평선 값이 없다")
  void getMovingAveragesSkipsDaysWithoutFullWindow() {
    Stock stock = saveStock("000011");
    LocalDate start = LocalDate.of(2026, 1, 1);
    saveCandles(stock, start, 30, i -> 1_000L);          // 30개(<50)

    MovingAverageResponse response =
        stockChartService.getMovingAverages(stock.getCode(), MA_50, start, start.plusDays(29));

    assertThat(response.dataPoints()).isEmpty();
  }

  @Test
  @DisplayName("베이스는 지지/저항 가격을 반환하고 최신 베이스의 endDate는 null이다")
  void getBasesMapsCurrentBaseWithNullEndDate() {
    Stock stock = saveStock("000013");
    saveBase(stock, 10_000L, 8_000L);

    BaseListResponse response = stockChartService.getBases(
        stock.getCode(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

    assertThat(response.bases()).hasSize(1);
    BaseItem item = response.bases().get(0);
    assertThat(item.endDate()).isNull();
    assertThat(item.supportPrice()).isEqualTo(8_000L);
    assertThat(item.resistancePrice()).isEqualTo(10_000L);
    assertThat(item.startDate()).isNotNull();
  }

  private Stock saveStock(String code) {
    return stockRepository.save(new Stock("종목" + code, code, BREAKOUT_READY, StockTrend.UPTREND));
  }

  private StockDailyCandle saveCandle(Stock stock, LocalDate date, long close) {
    return stockCandleRepository.save(StockDailyCandle.create(
        stock, date.format(DateTimeFormatter.BASIC_ISO_DATE), close, close, close, close, 1_000L, "2"));
  }

  private void saveCandles(Stock stock, LocalDate start, int count, IntToLongFunction closeFn) {
    for (int i = 0; i < count; i++) {
      saveCandle(stock, start.plusDays(i), closeFn.applyAsLong(i));
    }
  }

  private void saveBase(Stock stock, long resistancePrice, long supportPrice) {
    StockPricePoint high = new StockPricePoint(resistancePrice, 100_000L, LocalDate.now().minusDays(10),
        PIVOT_HIGH, null, stock);
    StockPricePoint low = new StockPricePoint(supportPrice, 100_000L, LocalDate.now().minusDays(20),
        PIVOT_LOW, null, stock);
    stockBaseRepository.save(StockBase.initOrLower(high, low, 100_000L));
  }
}
