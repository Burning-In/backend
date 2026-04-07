package com.momentum.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.StockState;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPivotType;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.service.StockPivotService;
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
class StockPivotServiceImplTest {

  @Autowired
  private StockPivotService stockPivotService;

  @Autowired
  private StockCandleRepository stockCandleRepository;

  @Autowired
  private StockRepository stockRepository;

  @Test
  @DisplayName("Pivot Low (-++) 조건이면 PIVOT_LOW로 변경된다")
  void determineDailyPivot_low() {
    // given
    String stockCode = "005930";

    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));

    String baseDate = "20250327";

    // T-2 (저점)
    List<StockCandle> dailyCandles = List.of(
        StockCandle.daily(stock, "20250325",
            100L, // open
            105L, // high
            95L,  // low
            100L, // close
            1000L, // volume
            "5" // DOWN
        ),
        StockCandle.daily(
            stock,
            "20250326",
            100L,
            110L,
            98L,
            105L,
            1200L,
            "2" // UP
        ),
        StockCandle.daily(
            stock,
            baseDate,
            105L,
            115L,
            104L,
            110L,
            1500L,
            "1" // UPPER_LIMIT
        ));
    stockCandleRepository.saveAll(dailyCandles);

    // when
    StockCandle result = stockPivotService.determineDailyPivot(stockCode, LocalDate.parse(baseDate, DateTimeFormatter.BASIC_ISO_DATE));

    // then
    assertThat(result.getStockPivotType()).isEqualTo(StockPivotType.PIVOT_LOW);
  }

  @Test
  @DisplayName("Pivot High (+--) 조건이면 PIVOT_HIGH로 변경된다")
  void determineDailyPivot_high() {
    // given
    String stockCode = "005930";

    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));

    String baseDate = "20250327";

    List<StockCandle> dailyCandles = List.of(
        // T-2 (고점)
        StockCandle.daily(
            stock,
            "20250325",
            110L,
            115L,
            108L,
            110L,
            1000L,
            "2" // UP
        ),
        // T-1
        StockCandle.daily(
            stock,
            "20250326",
            110L,
            112L,
            95L,
            95L,
            1200L,
            "5" // DOWN
        ),
        // T
        StockCandle.daily(
            stock,
            baseDate,
            95L,
            97L,
            90L,
            90L,
            1500L,
            "5" // DOWN
        )
    );

    stockCandleRepository.saveAll(dailyCandles);

    // when
    StockCandle result = stockPivotService.determineDailyPivot(
        stockCode,
        LocalDate.parse(baseDate, DateTimeFormatter.BASIC_ISO_DATE)
    );

    // then
    assertThat(result.getStockPivotType()).isEqualTo(StockPivotType.PIVOT_HIGH);
  }

  @Test
  @DisplayName("조건을 만족하지 않으면 FLAT")
  void determineDailyPivot_flat() {
    // given
    String stockCode = "005930";

    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));

    String baseDate = "20250327";

    List<StockCandle> dailyCandles = List.of(
        // T-2
        StockCandle.daily(
            stock,
            "20250325",
            100L,
            102L,
            99L,
            100L,
            1000L,
            "5" // DOWN
        ),
        // T-1 (미세 상승)
        StockCandle.daily(
            stock,
            "20250326",
            100L,
            103L,
            99L,
            101L,
            1200L,
            "2" // UP
        ),
        // T (또 미세 상승)
        StockCandle.daily(
            stock,
            baseDate,
            101L,
            104L,
            100L,
            102L,
            1500L,
            "2" // UP
        )
    );

    stockCandleRepository.saveAll(dailyCandles);

    // when
    StockCandle result = stockPivotService.determineDailyPivot(
        stockCode,
        LocalDate.parse(baseDate, DateTimeFormatter.BASIC_ISO_DATE)
    );

    // then
    assertThat(result.getStockPivotType()).isEqualTo(StockPivotType.FLAT);
  }
}
