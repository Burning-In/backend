package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.movingaverage.StockMovingAverage;
import com.momentum.domain.movingaverage.StockMovingAveragePeriod;
import com.momentum.domain.movingaverage.StockMovingAverageRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.MovingAverageResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MovingAverageInsightServiceTest {

  @Autowired MovingAverageInsightService movingAverageInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired StockCandleRepository stockCandleRepository;
  @Autowired StockMovingAverageRepository stockMovingAverageRepository;

  private static final LocalDate TODAY = LocalDate.now();
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    saveCandle(10000L);
  }

  @Test
  @DisplayName("현재가 > MA50 > MA150 > MA200 이면 정배열 true")
  void isAlignedTrueWhenPricesInOrder() {
    saveMas(9000L, 8000L, 7000L);

    MovingAverageResponse result = movingAverageInsightService.query(stock.getCode(), TODAY);

    assertThat(result.isAboveMa50()).isTrue();
    assertThat(result.isMa50AboveMa150()).isTrue();
    assertThat(result.isMa150AboveMa200()).isTrue();
    assertThat(result.currentPrice()).isEqualTo(10000L);
    assertThat(result.ma50()).isEqualTo(9000L);
    assertThat(result.ma150()).isEqualTo(8000L);
    assertThat(result.ma200()).isEqualTo(7000L);
  }

  @Test
  @DisplayName("현재가가 MA50보다 낮으면 정배열 false")
  void isAlignedFalseWhenPriceBelowMa50() {
    saveMas(11000L, 8000L, 7000L);

    MovingAverageResponse result = movingAverageInsightService.query(stock.getCode(), TODAY);

    assertThat(result.isAboveMa50()).isFalse();
  }

  @Test
  @DisplayName("MA50이 MA150보다 낮으면 정배열 false")
  void isAlignedFalseWhenMa50BelowMa150() {
    saveMas(9000L, 9500L, 7000L);

    MovingAverageResponse result = movingAverageInsightService.query(stock.getCode(), TODAY);

    assertThat(result.isMa50AboveMa150()).isFalse();
  }

  @Test
  @DisplayName("MA150이 MA200보다 낮으면 정배열 false")
  void isAlignedFalseWhenMa150BelowMa200() {
    saveMas(9000L, 8000L, 8500L);

    MovingAverageResponse result = movingAverageInsightService.query(stock.getCode(), TODAY);

    assertThat(result.isMa150AboveMa200()).isFalse();
  }

  @Test
  @DisplayName("MA 데이터 없으면 정배열 false, MA 값 null")
  void isAlignedFalseWhenNoMaData() {
    MovingAverageResponse result = movingAverageInsightService.query(stock.getCode(), TODAY);

    assertThat(result.isAboveMa50()).isFalse();
    assertThat(result.isMa50AboveMa150()).isFalse();
    assertThat(result.isMa150AboveMa200()).isFalse();
    assertThat(result.ma50()).isNull();
    assertThat(result.ma150()).isNull();
    assertThat(result.ma200()).isNull();
  }

  private void saveCandle(long closePrice) {
    String rawDate = TODAY.format(DateTimeFormatter.BASIC_ISO_DATE);
    stockCandleRepository.save(
        StockDailyCandle.create(stock, rawDate, closePrice, closePrice, closePrice, closePrice, 100000L, "2")
    );
  }

  private void saveMas(long ma50, long ma150, long ma200) {
    stockMovingAverageRepository.saveAll(List.of(
        new StockMovingAverage(ma50, StockMovingAveragePeriod.MA_50, stock),
        new StockMovingAverage(ma150, StockMovingAveragePeriod.MA_150, stock),
        new StockMovingAverage(ma200, StockMovingAveragePeriod.MA_200, stock)
    ));
  }
}
