package com.momentum.domain.stockcandle;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockCandleServiceTest {

  @Autowired
  private StockCandleService stockCandleService;

  @Autowired
  private StockRepository stockRepository;

  @Test
  @DisplayName("종목코드에 해당하는 일봉들을 저장한다")
  void create_shouldSaveCandles() {
    // given
    String stockCode = "005930";
    stockRepository.save(Stock.of("삼성전자", stockCode, StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    List<StockCandleCommand> commands = List.of(
        StockCandleCommand.of("20240101", 100L, 110L, 90L, 105L, 100L),
        StockCandleCommand.of("20240102", 105L, 115L, 95L, 110L, 100L)
    );

    // when
    List<StockDailyCandle> result = stockCandleService.create(stockCode, commands);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result).hasSize(2);
      softly.assertThat(result)
          .extracting(
              StockDailyCandle::getTradeDate,
              candle -> candle.getStock().getCode())
          .containsExactlyInAnyOrder(
              Tuple.tuple(LocalDate.of(2024, 1, 1), stockCode),
              Tuple.tuple(LocalDate.of(2024, 1, 2), stockCode));
    });
  }

  @Test
  @DisplayName("존재하지 않는 종목코드면 IllegalArgumentException")
  void create_throwsWhenStockNotFound() {
    List<StockCandleCommand> commands = List.of(
        StockCandleCommand.of("20240101", 100L, 110L, 90L, 105L, 100L));

    assertThatThrownBy(() -> stockCandleService.create("999999", commands))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
