package com.momentum.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.application.dto.StockTickInfo;
import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCode;
import com.momentum.domain.entity.StockState;
import com.momentum.domain.entity.StockTick;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.entity.indicator.price.StockLine;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockLineRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.respository.StockTickRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockStateServiceTest {

  @Autowired
  private StockStateService stockStateService;

  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockLineRepository stockLineRepository;

  @Autowired
  private StockBaseRepository stockBaseRepository;

  @Autowired
  private StockTickRepository stockTickRepository;

  // -------------------------------
  // 공통 헬퍼
  // -------------------------------
  private Stock createStock() {
    return stockRepository.save(new Stock("삼성전자", "005930", StockState.UNDEFIED, StockTrend.UPTREND));
  }

  private StockBase createBase(Stock stock, long resistance, long support) {
    // 1. 저항선으로 candidate 시작
    StockLine resistanceLine = StockLine.resistance(resistance, stock);
    stockLineRepository.save(resistanceLine);
    StockBase base = StockBase.createCandidate(stock, resistanceLine, 0L);
    stockBaseRepository.save(base);

    // 2. 지지선으로 confirm
    StockLine supportLine = StockLine.support(support, stock);
    stockLineRepository.save(supportLine);
    base.confirm(supportLine);

    return stockBaseRepository.save(base);
  }

  private void saveTick(Instant time, long price, double strength, Stock stock) {
    stockTickRepository.save(
        new StockTick(String.valueOf(time), price, 100L, 100L, strength, StockCode.getCode(stock.getCode()))
    );
  }

  private StockTickInfo createTickInfo(
      Instant now,
      long current,
      long low,
      long high,
      double strength
  ) {
    return new StockTickInfo(
        "005930",
        String.valueOf(now),
        current,
        "+",
        10,
        10,
        10,
        low,
        high,
        low,
        100L,
        500L,
        100L,
        200L,
        strength
    );
  }

  // -------------------------------
  // 1. BREAKOUT
  // -------------------------------
  @Test
  @DisplayName("상승추세 + 저항 돌파 + 체결강도 증가 → BREAKOUT")
  void processTick_breakout() {
    // given
    Stock stock = createStock();

    createBase(stock, 100_000L, 90_000L);

    Instant now = Instant.now();

    // 장 시작
    saveTick(now.minusSeconds(20), 90_000L, 50.0, stock);

    // 저항 이전 (약함)
    saveTick(now.minusSeconds(10), 95_000L, 50.0, stock);

    // 저항 이후 (강함)
    saveTick(now.minusSeconds(5), 101_000L, 200.0, stock);

    StockTickInfo tickInfo = createTickInfo(
        now,
        102_000L,
        88_000L,
        103_000L,
        250.0
    );

    // when
    stockStateService.processTick(tickInfo, now);

    // then
    assertThat(stock.getStockState()).isEqualTo(StockState.BREAKOUT);
  }

  // -------------------------------
  // 2. FAILED_BREAKOUT
  // -------------------------------
  @Test
  @DisplayName("저항 아래 하락 + 위에서 약함 → FAILED_BREAKOUT")
  void processTick_failedBreakout() {
    // given
    Stock stock = createStock();

    createBase(stock, 100_000L, 90_000L);

    Instant now = Instant.now();

    // 장 시작
    saveTick(now.minusSeconds(20), 90_000L, 100.0, stock);

    // 저항 위 (약함)
    saveTick(now.minusSeconds(10), 101_000L, 30.0, stock);

    // 저항 아래 (강함)
    saveTick(now.minusSeconds(5), 95_000L, 150.0, stock);

    StockTickInfo tickInfo = createTickInfo(
        now,
        96_000L,
        90_000L,
        102_000L,
        80.0D
    );

    // when
    stockStateService.processTick(tickInfo, now);

    // then
    assertThat(stock.getStockState()).isEqualTo(StockState.FAILED_BREAKOUT);
  }

  // -------------------------------
  // 3. BREAKDOWN
  // -------------------------------
  @Test
  @DisplayName("지지선 붕괴 + 아래에서 약함 → BREAKDOWN")
  void processTick_breakdown() {
    // given
    Stock stock = createStock();

    createBase(stock, 100_000L, 90_000L);

    Instant now = Instant.now();

    // 장 시작
    saveTick(now.minusSeconds(20), 95_000L, 100.0, stock);

    // 지지 위 (강함)
    saveTick(now.minusSeconds(10), 92_000L, 150.0, stock);

    // 지지 아래 (약함)
    saveTick(now.minusSeconds(5), 85_000L, 20.0, stock);

    StockTickInfo tickInfo = createTickInfo(
        now,
        87_000L,
        80_000L,
        96_000L,
        30.0
    );

    // when
    stockStateService.processTick(tickInfo, now);

    // then
    assertThat(stock.getStockState()).isEqualTo(StockState.BREAKDOWN);
  }
}
