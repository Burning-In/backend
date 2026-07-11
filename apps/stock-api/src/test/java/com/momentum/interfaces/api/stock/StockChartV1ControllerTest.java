package com.momentum.interfaces.api.stock;

import static com.momentum.domain.pricepoint.entity.StockPricePointType.HIGH;
import static com.momentum.domain.pricepoint.entity.StockPricePointType.LOW;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StockChartV1ControllerTest {

  @Autowired
  private MockMvcTester mockMvcTester;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockBaseRepository stockBaseRepository;

  @Test
  @DisplayName("일봉 조회 API 해피케이스")
  void getDailyCandle() {
    Stock stock = saveStock("000520");
    saveCandle(stock, LocalDate.of(2026, 5, 1), 100L);
    saveCandle(stock, LocalDate.of(2026, 5, 2), 200L);
    saveCandle(stock, LocalDate.of(2026, 5, 3), 300L);

    assertThat(mockMvcTester.get().uri("/api/v1/stocks/{code}/chart/daily", "000520")
        .param("from", "2026-05-02")
        .param("to", "2026-05-03T00:00:00"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.candles[0].closePrice").isEqualTo(200);
  }

  @Test
  @DisplayName("이평선 조회 API 해피케이스 — 롤링 SMA")
  void getMovingAverages() {
    Stock stock = saveStock("000080");
    LocalDate start = LocalDate.of(2026, 1, 1);
    for (int i = 0; i < 50; i++) {
      saveCandle(stock, start.plusDays(i), 1_000L);   // index 0~49
    }
    saveCandle(stock, start.plusDays(50), 2_000L);     // index 50

    var result = mockMvcTester.get().uri("/api/v1/stocks/{code}/chart/moving-averages", "000080")
        .param("period", "MA_50")
        .param("from", "2026-02-19")
        .param("to", "2026-02-20T00:00:00")
        .exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.data.dataPoints[0].price").isEqualTo(1000);  // 50개 평균
    assertThat(result).bodyJson().extractingPath("$.data.dataPoints[1].price").isEqualTo(1020);  // (49*1000+2000)/50
  }

  @Test
  @DisplayName("베이스 조회 API 해피케이스")
  void getBases() {
    Stock stock = saveStock("000540");
    saveBase(stock, 10_000L, 8_000L);

    var result = mockMvcTester.get().uri("/api/v1/stocks/{code}/chart/bases", "000540")
        .param("from", "2000-01-01")
        .param("to", "2100-01-01T00:00:00")
        .exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.data.bases[0].resistancePrice").isEqualTo(10000);
    assertThat(result).bodyJson().extractingPath("$.data.bases[0].endDate").isNull();  // 유일 베이스 = 진행 중
  }

  private Stock saveStock(String code) {
    return stockRepository.save(Stock.of("종목" + code, code, BREAKOUT_READY, StockTrend.UPTREND));
  }

  private void saveCandle(Stock stock, LocalDate date, long close) {
    stockCandleRepository.save(StockDailyCandle.create(
        stock, date.format(DateTimeFormatter.BASIC_ISO_DATE), close, close, close, close, 1_000L, "2"));
  }

  private void saveBase(Stock stock, long resistancePrice, long supportPrice) {
    StockPricePoint high = new StockPricePoint(resistancePrice, 100_000L, LocalDate.now().minusDays(10),
        HIGH, null, stock);
    StockPricePoint low = new StockPricePoint(supportPrice, 100_000L, LocalDate.now().minusDays(20),
        LOW, null, stock);
    stockBaseRepository.save(StockBase.init(high, low, 100_000L));
  }
}
