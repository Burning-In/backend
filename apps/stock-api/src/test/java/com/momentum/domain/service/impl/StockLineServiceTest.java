package com.momentum.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.application.StockLineService;
import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.StockState;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockLine;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockLineRepository;
import com.momentum.domain.respository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockLineServiceTest {

  @Autowired
  private StockLineService stockLineService;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockLineRepository stockLineRepository;

  @Test
  @DisplayName("유사저항선이 범위안에 없으면, 새로 생성한다")
  void determineResistance() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));
    StockLine existing = StockLine.resistance(100_800L, stock);
    stockLineRepository.save(existing);
    StockCandle candle = StockCandle.daily(
        stock,
        "20250801",
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        "1"
    );
    stockCandleRepository.save(candle);

    // when
    StockLine result = stockLineService.determineResistance(stock, candle, 0.7);

    // then
    assertThat(result).extracting(
            StockLine::getPrice,
            StockLine::getResistanceTouchCount)
        .containsExactlyInAnyOrder(100_000L, 1L);
  }

  @Test
  @DisplayName("유사저항선 범위안에 있으면, 범위안의 값중 최고값을 반환한다")
  void determineResistance_haveResistance() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));
    StockLine existingHigh = StockLine.resistance(100_600L, stock);
    StockLine existingLow = StockLine.resistance(100_100L, stock);
    stockLineRepository.save(existingHigh);
    stockLineRepository.save(existingLow);
    StockCandle candle = StockCandle.daily(
        stock,
        "20250801",
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        "1"
    );
    stockCandleRepository.save(candle);

    // when
    StockLine result = stockLineService.determineResistance(stock, candle, 0.7);

    // then
    assertThat(result).extracting(
            StockLine::getPrice,
            StockLine::getResistanceTouchCount)
        .containsExactlyInAnyOrder(existingHigh.getPrice(), 2L);
  }

  @Test
  @DisplayName("유사지지선이 범위안에 없으면, 새로 생성한다")
  void determineSupport_createNew() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));
    StockLine existing = StockLine.support(99_000L, stock);
    stockLineRepository.save(existing);

    StockCandle candle = StockCandle.daily(
        stock,
        "20250801",
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        "1"
    );
    stockCandleRepository.save(candle);

    // when
    StockLine result = stockLineService.determineSupport(stock, candle, 0.7);

    // then
    assertThat(result).extracting(
            StockLine::getPrice,
            StockLine::getSupportTouchCount)
        .containsExactlyInAnyOrder(100_000L, 1L);
  }

  @Test
  @DisplayName("유사지지선 범위안에 있으면, 범위안의 값중 최저값을 반환한다")
  void determineSupport_haveSupport() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));
    StockLine existingHigh = StockLine.support(100_600L, stock);
    StockLine existingLow = StockLine.support(99_400L, stock);
    stockLineRepository.save(existingHigh);
    stockLineRepository.save(existingLow);

    StockCandle candle = StockCandle.daily(
        stock,
        "20250801",
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        100_000L,
        "1"
    );
    stockCandleRepository.save(candle);

    // when
    StockLine result = stockLineService.determineSupport(stock, candle, 0.7);

    // then
    assertThat(result).extracting(
            StockLine::getPrice,
            StockLine::getSupportTouchCount)
        .containsExactlyInAnyOrder(existingLow.getPrice(), 2L);
  }
}
