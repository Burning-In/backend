package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockBaseStageLevelAdjusterTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2026, 5, 20);
  private static final long VOLUME = 100_000L;

  @Autowired
  private StockBaseStageLevelAdjuster stockBaseStageLevelAdjuster;
  @Autowired
  private StockAnchorPointRepository stockAnchorPointRepository;
  @Autowired
  private StockBaseRepository stockBaseRepository;
  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("이전 베이스가 없으면 단계를 건드리지 않는다")
  void resolve_withoutPreviousBase_keepsStageLevel() {
    // given
    StockBase currentBase = saveBase(15_000L, 12_000L, TRADE_DATE.minusDays(10), 3L);
    StockAnchorPoint droppedLow = savePoint(8_000L, TRADE_DATE, LOW);

    // when
    stockBaseStageLevelAdjuster.resolve(droppedLow, currentBase);

    // then
    assertThat(currentBase.getStageLevel()).isEqualTo(3L);
  }

  @Test
  @DisplayName("저점이 이전 베이스까지 내려가면 이전 베이스의 단계로 되돌린다")
  void resolve_whenLowDroppedToPreviousBase_restoresPreviousStageLevel() {
    // given
    saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(30), 1L);
    StockBase currentBase = saveBase(15_000L, 12_000L, TRADE_DATE.minusDays(10), 3L);
    StockAnchorPoint droppedLow = savePoint(9_000L, TRADE_DATE, LOW);

    // when
    stockBaseStageLevelAdjuster.resolve(droppedLow, currentBase);

    // then
    assertThat(currentBase.getStageLevel()).isEqualTo(1L);
  }

  @Test
  @DisplayName("고점이 이전 베이스까지 올라가면 이전 베이스의 단계로 되돌린다")
  void resolve_whenHighRaisedToPreviousBase_restoresPreviousStageLevel() {
    // given
    saveBase(20_000L, 18_000L, TRADE_DATE.minusDays(30), 5L);
    StockBase currentBase = saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(10), 2L);
    StockAnchorPoint raisedHigh = savePoint(21_000L, TRADE_DATE, HIGH);

    // when
    stockBaseStageLevelAdjuster.resolve(raisedHigh, currentBase);

    // then
    assertThat(currentBase.getStageLevel()).isEqualTo(5L);
  }

  @Test
  @DisplayName("현재 베이스 안에 머무는 점이면 단계를 건드리지 않는다")
  void resolve_whenPointStaysInsideCurrentBase_keepsStageLevel() {
    // given
    saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(30), 1L);
    StockBase currentBase = saveBase(15_000L, 12_000L, TRADE_DATE.minusDays(10), 3L);
    StockAnchorPoint insideLow = savePoint(13_000L, TRADE_DATE, LOW);

    // when
    stockBaseStageLevelAdjuster.resolve(insideLow, currentBase);

    // then
    assertThat(currentBase.getStageLevel()).isEqualTo(3L);
  }

  private StockBase saveBase(long highPrice, long lowPrice, LocalDate tradeDate, long stageLevel) {
    StockAnchorPoint high = savePoint(highPrice, tradeDate, HIGH);
    StockAnchorPoint low = savePoint(lowPrice, tradeDate.plusDays(1), LOW);
    StockBase base = StockBase.init(high, low, VOLUME);
    base.update(stageLevel, null);
    return stockBaseRepository.save(base);
  }

  private StockAnchorPoint savePoint(long price, LocalDate tradeDate, StockAnchorPointType type) {
    return stockAnchorPointRepository.save(new StockAnchorPoint(price, VOLUME, tradeDate, type, null, stock));
  }
}
