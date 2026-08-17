package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseLineType;
import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockBaseConfirmerTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2026, 5, 20);
  private static final long VOLUME = 100_000L;

  private static final long BASE_RESISTANCE = 11_000L;
  private static final long BASE_SUPPORT = 10_000L;

  @Autowired
  private StockBaseConfirmer stockBaseConfirmer;
  @Autowired
  private StockAnchorPointRepository stockAnchorPointRepository;
  @Autowired
  private StockBaseRepository stockBaseRepository;
  @Autowired
  private StockRepository stockRepository;

  private Stock stock;
  private StockBase currentBase;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.UPTREND));
    StockAnchorPoint baseHigh = savePoint(BASE_RESISTANCE, TRADE_DATE.minusDays(20), HIGH);
    StockAnchorPoint baseLow = savePoint(BASE_SUPPORT, TRADE_DATE.minusDays(19), LOW);
    currentBase = stockBaseRepository.save(StockBase.init(baseHigh, baseLow, VOLUME));
  }

  @Test
  @DisplayName("저점이 저항선 위로 올라오면 단계가 한 칸 오른 새 베이스를 만든다")
  void resolve_whenLowRoseAboveResistance_createsUpperBase() {
    // given
    savePoint(14_000L, TRADE_DATE.minusDays(2), HIGH);
    StockAnchorPoint confirmedLow = savePoint(12_000L, TRADE_DATE, LOW);

    // when
    StockBase newBase = stockBaseConfirmer.resolve(confirmedLow, currentBase);

    // then
    assertThat(newBase).isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(newBase.getStageLevel()).isEqualTo(currentBase.getStageLevel() + 1);
      softly.assertThat(newBase.getHighestResistanceLine().getPrice()).isEqualTo(14_000L);
      softly.assertThat(newBase.getLowestSupportLine().getPrice()).isEqualTo(12_000L);
    });
  }

  @Test
  @DisplayName("고점이 지지선 아래로 내려가면 단계가 한 칸 내린 새 베이스를 만든다")
  void resolve_whenHighFellBelowSupport_createsLowerBase() {
    // given
    savePoint(7_000L, TRADE_DATE.minusDays(2), LOW);
    StockAnchorPoint confirmedHigh = savePoint(8_000L, TRADE_DATE, HIGH);

    // when
    StockBase newBase = stockBaseConfirmer.resolve(confirmedHigh, currentBase);

    // then
    assertThat(newBase).isNotNull();
    assertSoftly(softly -> {
      softly.assertThat(newBase.getStageLevel()).isEqualTo(currentBase.getStageLevel() - 1);
      softly.assertThat(newBase.getHighestResistanceLine().getPrice()).isEqualTo(8_000L);
      softly.assertThat(newBase.getHighestResistanceLine().getType()).isEqualTo(StockBaseLineType.RESISTANCE);
      softly.assertThat(newBase.getLowestSupportLine().getPrice()).isEqualTo(7_000L);
      softly.assertThat(newBase.getLowestSupportLine().getType()).isEqualTo(StockBaseLineType.SUPPORT);
    });
  }

  @Test
  @DisplayName("베이스 안에 머무는 점이면 새 베이스를 만들지 않는다")
  void resolve_whenPointStaysInsideBase_createsNothing() {
    // given
    StockAnchorPoint confirmedLow = savePoint(10_400L, TRADE_DATE, LOW);

    // when
    StockBase newBase = stockBaseConfirmer.resolve(confirmedLow, currentBase);

    // then
    assertThat(newBase).isNull();
  }

  @Test
  @DisplayName("짝이 될 점이 없으면 새 베이스를 만들지 않는다")
  void resolve_withoutPairedPoint_createsNothing() {
    // given
    StockAnchorPoint confirmedLow = savePoint(12_000L, TRADE_DATE, LOW);

    // when
    StockBase newBase = stockBaseConfirmer.resolve(confirmedLow, currentBase);

    // then
    assertThat(newBase).isNull();
  }

  private StockAnchorPoint savePoint(long price, LocalDate tradeDate, StockAnchorPointType type) {
    return stockAnchorPointRepository.save(new StockAnchorPoint(price, VOLUME, tradeDate, type, null, stock));
  }
}
