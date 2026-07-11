package com.momentum.domain.stockcandle;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
class StockCandleServiceTest {

  @Autowired
  private StockCandleService stockCandleService;

  @Autowired
  private StockRepository stockRepository;

  @Transactional
  @Test
  void create_shouldSaveCandles() {
    // given
    String stockCode = "005930";

    Stock stock = Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND);
    stockRepository.save(stock);
    List<StockCandleDto> candleResponses = List.of(
        new StockCandleDto("20240101", 100L, 110L, 90L, 105L, 100, 100, "1"),
        new StockCandleDto("20240102", 105L, 115L, 95L, 110L, 100, 100, "1")
    );

    // when
    List<StockDailyCandle> result = stockCandleService.create(stockCode, candleResponses);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(
            StockDailyCandle::getTradeDate,
            s -> s.getStock().getCode()
        )
        .containsExactlyInAnyOrder(
            Tuple.tuple(LocalDate.of(2024, 1, 1), stockCode),
            Tuple.tuple(LocalDate.of(2024, 1, 2), stockCode)
        );
  }
}
