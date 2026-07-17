package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.StockRegimeResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RegimeInsightServiceTest {

  @Autowired RegimeInsightService regimeInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired StockCandleRepository stockCandleRepository;
  @Autowired StockBaseRepository stockBaseRepository;
  @Autowired StockPricePointRepository stockPricePointRepository;

  private static final LocalDate TODAY = LocalDate.now();

  @Test
  @DisplayName("베이스 없으면 UNKNOWN 반환")
  void returnsUndeterminedWhenNoBase() {
    Stock stock = saveStock("005930", StockRegime.UNKNOWN, StockTrend.UPTREND);
    saveCandle(stock, TODAY, 10000L);

    StockRegimeResponse result = regimeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.regime()).isEqualTo(StockRegime.UNKNOWN);
    assertThat(result.currentPrice()).isEqualTo(10000L);
    assertThat(result.supportLine()).isNull();
    assertThat(result.resistanceLine()).isNull();
    assertThat(result.changeRateFromReferenceLine()).isNull();
  }

  @Test
  @DisplayName("BREAKOUT_SUCCESS 레짐이면 저항선 대비 변동률 반환")
  void returnsChangeRateFromResistanceWhenBreakoutSuccess() {
    Stock stock = saveStock("000040", StockRegime.BREAKOUT_SUCCESS, StockTrend.UPTREND);
    saveCandle(stock, TODAY, 11000L);
    saveBase(stock, 10000L, 8000L);

    StockRegimeResponse result = regimeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.regime()).isEqualTo(StockRegime.BREAKOUT_SUCCESS);
    assertThat(result.resistanceLine()).isEqualTo(10000L);
    assertThat(result.supportLine()).isEqualTo(8000L);
    // (11000 - 10000) / 10000 * 100 = 10.0
    assertThat(result.changeRateFromReferenceLine()).isEqualByComparingTo(new BigDecimal("10.0000"));
  }

  @Test
  @DisplayName("DOWNSIDE_BREAK 레짐이면 지지선 대비 변동률 반환")
  void returnsChangeRateFromSupportWhenDownsideBreak() {
    Stock stock = saveStock("000050", StockRegime.DOWNSIDE_BREAK, StockTrend.UPTREND);
    saveCandle(stock, TODAY, 9000L);
    saveBase(stock, 12000L, 10000L);

    StockRegimeResponse result = regimeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.regime()).isEqualTo(StockRegime.DOWNSIDE_BREAK);
    // (9000 - 10000) / 10000 * 100 = -10.0
    assertThat(result.changeRateFromReferenceLine()).isEqualByComparingTo(new BigDecimal("-10.0000"));
  }

  @Test
  @DisplayName("BREAKOUT_FAILED 레짐이면 저항선 대비 음수 변동률 반환")
  void returnsNegativeChangeRateFromResistanceWhenBreakoutFailed() {
    Stock stock = saveStock("000070", StockRegime.BREAKOUT_FAILED, StockTrend.UPTREND);
    saveCandle(stock, TODAY, 9500L);
    saveBase(stock, 10000L, 8000L);

    StockRegimeResponse result = regimeInsightService.query(stock.getCode(), TODAY);

    assertThat(result.regime()).isEqualTo(StockRegime.BREAKOUT_FAILED);
    // (9500 - 10000) / 10000 * 100 = -5.0
    assertThat(result.changeRateFromReferenceLine()).isEqualByComparingTo(new BigDecimal("-5.0000"));
  }

  private Stock saveStock(String code, StockRegime regime, StockTrend trend) {
    return stockRepository.save(Stock.of("테스트종목", code, regime, trend));
  }

  private void saveCandle(Stock stock, LocalDate date, long closePrice) {
    String rawDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
    stockCandleRepository.save(
        StockDailyCandle.create(stock, rawDate, closePrice, closePrice, closePrice, closePrice, 100000L)
    );
  }

  private void saveBase(Stock stock, long highPrice, long lowPrice) {
    StockPricePoint high = stockPricePointRepository.save(
        new StockPricePoint(highPrice, 100000L, TODAY.minusDays(10), StockPricePointType.HIGH, null, stock)
    );
    StockPricePoint low = stockPricePointRepository.save(
        new StockPricePoint(lowPrice, 100000L, TODAY.minusDays(20), StockPricePointType.LOW, null, stock)
    );
    StockBase base = StockBase.init(high, low, 100000L);
    stockBaseRepository.save(base);
  }
}
