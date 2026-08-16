package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.ASCENDING;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockBaseServiceTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2026, 5, 20);
  private static final long VOLUME = 100_000L;

  private static final long BASE_LOW = 10_000L;
  private static final long BASE_HIGH = 15_000L;
  private static final long BASE_STAGE_LEVEL = 1L;

  private static final long LOW_INSIDE_BASE = 12_500L;
  private static final long LOW_ABOVE_BASE = 17_000L;
  private static final long PAIRED_HIGH_ABOVE_BASE = 20_000L;
  private static final long HIGH_BELOW_BASE = 8_000L;
  private static final long PAIRED_LOW_BELOW_BASE = 6_000L;

  private static final long OLDEST_BASE_LOW = 10_000L;
  private static final long OLDEST_BASE_HIGH = 11_500L;
  private static final long OLDEST_BASE_STAGE_LEVEL = 1L;

  private static final long PREVIOUS_BASE_LOW = 14_000L;
  private static final long PREVIOUS_BASE_HIGH = 16_100L;
  private static final long PREVIOUS_BASE_STAGE_LEVEL = 2L;

  private static final long CURRENT_BASE_LOW = 20_000L;
  private static final long CURRENT_BASE_HIGH = 23_000L;
  private static final long CURRENT_BASE_STAGE_LEVEL = 3L;

  private static final long LOW_INSIDE_CURRENT_BASE = 21_500L;
  private static final long LOW_REVERTED_TO_PREVIOUS_BASE = 15_000L;
  private static final long LOW_ABOVE_CURRENT_BASE = 25_000L;
  private static final long PAIRED_HIGH_ABOVE_CURRENT_BASE = 29_000L;
  private static final long HIGH_BELOW_CURRENT_BASE = 18_000L;
  private static final long PAIRED_LOW_BELOW_CURRENT_BASE = 14_000L;

  @Autowired
  private StockBaseService stockBaseService;
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
  @DisplayName("확정된 점이 없으면 예외가 발생한다")
  void resolve_withoutPoint_throwsException() {
    assertThatThrownBy(() -> stockBaseService.resolve(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("고점도 저점도 아닌 점이면 베이스를 건드리지 않는다")
  void resolve_withNonPivotPoint_createsNothing() {
    // given
    savePoint(11_000L, TRADE_DATE.minusDays(5), HIGH);
    StockAnchorPoint ascending = savePoint(10_500L, TRADE_DATE, ASCENDING);

    // when
    stockBaseService.resolve(ascending);

    // then
    assertThat(allBases()).isEmpty();
  }

  @Test
  @DisplayName("베이스가 하나도 없으면 첫 베이스를 만든다")
  void resolve_withoutCurrentBase_createsFirstBase() {
    // given
    savePoint(11_000L, TRADE_DATE.minusDays(5), HIGH);
    StockAnchorPoint confirmedLow = savePoint(10_000L, TRADE_DATE, LOW);

    // when
    stockBaseService.resolve(confirmedLow);

    // then
    assertSoftly(softly -> {
      softly.assertThat(allBases()).hasSize(1);
      softly.assertThat(allBases().getFirst().getStageLevel()).isEqualTo(BASE_STAGE_LEVEL);
    });
  }

  @Test
  @DisplayName("베이스 안쪽 점이면 새 베이스를 만들지 않고 현재 베이스에 편입한다")
  void resolve_withPointInsideCurrentBase_integratesIntoCurrentBase() {
    // given
    saveOnlyBase();
    StockAnchorPoint insideLow = savePoint(LOW_INSIDE_BASE, TRADE_DATE, LOW);

    // when
    stockBaseService.resolve(insideLow);

    // then
    assertSoftly(softly -> {
      softly.assertThat(allBases()).hasSize(1);
      softly.assertThat(insideLow.getStockBase()).isNotNull();
    });
  }

  @Test
  @DisplayName("저점이 저항선 위로 올라오면 단계가 오른 새 베이스가 생긴다")
  void resolve_whenLowRoseAboveResistance_createsUpperBase() {
    // given
    saveOnlyBase();
    savePoint(PAIRED_HIGH_ABOVE_BASE, TRADE_DATE.minusDays(2), HIGH);
    StockAnchorPoint risenLow = savePoint(LOW_ABOVE_BASE, TRADE_DATE, LOW);

    // when
    stockBaseService.resolve(risenLow);

    // then
    List<StockBase> bases = allBases();
    assertSoftly(softly -> {
      softly.assertThat(bases).hasSize(2);
      softly.assertThat(bases.getLast().getStageLevel()).isEqualTo(BASE_STAGE_LEVEL + 1);
      softly.assertThat(bases.getLast().getHighestResistanceLine().getPrice()).isEqualTo(PAIRED_HIGH_ABOVE_BASE);
      softly.assertThat(bases.getLast().getLowestSupportLine().getPrice()).isEqualTo(LOW_ABOVE_BASE);
    });
  }

  @Test
  @DisplayName("고점이 지지선 아래로 내려가면 단계가 내린 새 베이스가 생긴다")
  void resolve_whenHighFellBelowSupport_createsLowerBase() {
    // given
    saveOnlyBase();
    savePoint(PAIRED_LOW_BELOW_BASE, TRADE_DATE.minusDays(2), LOW);
    StockAnchorPoint fallenHigh = savePoint(HIGH_BELOW_BASE, TRADE_DATE, HIGH);

    // when
    stockBaseService.resolve(fallenHigh);

    // then
    List<StockBase> bases = allBases();
    assertSoftly(softly -> {
      softly.assertThat(bases).hasSize(2);
      softly.assertThat(bases.getLast().getStageLevel()).isEqualTo(BASE_STAGE_LEVEL - 1);
      softly.assertThat(bases.getLast().getHighestResistanceLine().getPrice()).isEqualTo(HIGH_BELOW_BASE);
      softly.assertThat(bases.getLast().getLowestSupportLine().getPrice()).isEqualTo(PAIRED_LOW_BELOW_BASE);
    });
  }

  @Test
  @DisplayName("저점이 이전 베이스까지 되돌아가면 현재 베이스의 단계가 이전 베이스 단계로 내려간다")
  void resolve_whenLowRevertedToPreviousBase_restoresPreviousStageLevel() {
    // given
    StockBase currentBase = saveThreeBasesAndReturnCurrent();
    StockAnchorPoint revertedLow = savePoint(LOW_REVERTED_TO_PREVIOUS_BASE, TRADE_DATE, LOW);

    // when
    stockBaseService.resolve(revertedLow);

    // then
    assertSoftly(softly -> {
      softly.assertThat(allBases()).hasSize(3);
      softly.assertThat(currentBase.getStageLevel()).isEqualTo(PREVIOUS_BASE_STAGE_LEVEL);
    });
  }

  @Test
  @DisplayName("베이스가 여럿이면 가장 최근 베이스에 편입한다")
  void resolve_withManyBases_integratesIntoMostRecentBase() {
    // given
    StockBase currentBase = saveThreeBasesAndReturnCurrent();
    StockAnchorPoint insideLow = savePoint(LOW_INSIDE_CURRENT_BASE, TRADE_DATE, LOW);

    // when
    stockBaseService.resolve(insideLow);

    // then
    assertSoftly(softly -> {
      softly.assertThat(allBases()).hasSize(3);
      softly.assertThat(insideLow.getStockBase().getId()).isEqualTo(currentBase.getId());
    });
  }

  @Test
  @DisplayName("베이스가 여럿이면 가장 최근 베이스를 기준으로 상위 베이스를 만든다")
  void resolve_withManyBases_createsUpperBaseFromMostRecentBase() {
    // given
    saveThreeBasesAndReturnCurrent();
    savePoint(PAIRED_HIGH_ABOVE_CURRENT_BASE, TRADE_DATE.minusDays(2), HIGH);
    StockAnchorPoint risenLow = savePoint(LOW_ABOVE_CURRENT_BASE, TRADE_DATE, LOW);

    // when
    stockBaseService.resolve(risenLow);

    // then
    List<StockBase> bases = allBases();
    assertSoftly(softly -> {
      softly.assertThat(bases).hasSize(4);
      softly.assertThat(bases.getLast().getStageLevel()).isEqualTo(CURRENT_BASE_STAGE_LEVEL + 1);
      softly.assertThat(bases.getLast().getHighestResistanceLine().getPrice())
          .isEqualTo(PAIRED_HIGH_ABOVE_CURRENT_BASE);
      softly.assertThat(bases.getLast().getLowestSupportLine().getPrice()).isEqualTo(LOW_ABOVE_CURRENT_BASE);
    });
  }

  @Test
  @DisplayName("베이스가 여럿이면 가장 최근 베이스를 기준으로 하위 베이스를 만든다")
  void resolve_withManyBases_createsLowerBaseFromMostRecentBase() {
    // given
    saveThreeBasesAndReturnCurrent();
    savePoint(PAIRED_LOW_BELOW_CURRENT_BASE, TRADE_DATE.minusDays(2), LOW);
    StockAnchorPoint fallenHigh = savePoint(HIGH_BELOW_CURRENT_BASE, TRADE_DATE, HIGH);

    // when
    stockBaseService.resolve(fallenHigh);

    // then
    List<StockBase> bases = allBases();
    assertSoftly(softly -> {
      softly.assertThat(bases).hasSize(4);
      softly.assertThat(bases.getLast().getStageLevel()).isEqualTo(CURRENT_BASE_STAGE_LEVEL - 1);
      softly.assertThat(bases.getLast().getHighestResistanceLine().getPrice()).isEqualTo(HIGH_BELOW_CURRENT_BASE);
      softly.assertThat(bases.getLast().getLowestSupportLine().getPrice()).isEqualTo(PAIRED_LOW_BELOW_CURRENT_BASE);
    });
  }

  private List<StockBase> allBases() {
    return stockBaseRepository.findAllByStockOrderByStartedAt(stock);
  }

  private void saveOnlyBase() {
    saveBase(BASE_HIGH, BASE_LOW, TRADE_DATE.minusDays(20), BASE_STAGE_LEVEL);
  }

  private StockBase saveThreeBasesAndReturnCurrent() {
    saveBase(OLDEST_BASE_HIGH, OLDEST_BASE_LOW, TRADE_DATE.minusDays(60), OLDEST_BASE_STAGE_LEVEL);
    saveBase(PREVIOUS_BASE_HIGH, PREVIOUS_BASE_LOW, TRADE_DATE.minusDays(40), PREVIOUS_BASE_STAGE_LEVEL);
    return saveBase(CURRENT_BASE_HIGH, CURRENT_BASE_LOW, TRADE_DATE.minusDays(20), CURRENT_BASE_STAGE_LEVEL);
  }

  private StockBase saveBase(long highPrice, long lowPrice, LocalDate tradeDate, long stageLevel) {
    StockAnchorPoint high = savePoint(highPrice, tradeDate, HIGH);
    StockAnchorPoint low = savePoint(lowPrice, tradeDate.plusDays(1), LOW);
    StockBase base = StockBase.init(high, low, VOLUME);
    base.update(stageLevel, null);
    return stockBaseRepository.save(base);
  }

  private StockAnchorPoint highPoint(long price) {
    return new StockAnchorPoint(price, VOLUME, TRADE_DATE, HIGH, null, stock);
  }

  private StockAnchorPoint lowPoint(long price) {
    return new StockAnchorPoint(price, VOLUME, TRADE_DATE, LOW, null, stock);
  }

  private StockAnchorPoint savePoint(long price, LocalDate tradeDate, StockAnchorPointType type) {
    return stockAnchorPointRepository.save(new StockAnchorPoint(price, VOLUME, tradeDate, type, null, stock));
  }
}
