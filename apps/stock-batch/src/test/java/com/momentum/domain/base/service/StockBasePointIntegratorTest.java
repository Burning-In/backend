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
class StockBasePointIntegratorTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2026, 5, 20);
  private static final long VOLUME = 100_000L;

  private static final long BASE_RESISTANCE = 15_000L;
  private static final long BASE_SUPPORT = 10_000L;

  @Autowired
  private StockBasePointIntegrator stockBasePointIntegrator;
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
  @DisplayName("베이스 안쪽 저점이 들어오면 아직 베이스가 없는 점들이 베이스에 편입된다")
  void resolve_withLowInsideBase_assignsUnassignedPoints() {
    // given
    StockAnchorPoint insideLow = savePoint(12_000L, TRADE_DATE, LOW);

    // when
    stockBasePointIntegrator.resolve(insideLow, currentBase);

    // then
    assertSoftly(softly -> {
      softly.assertThat(insideLow.getStockBase()).isNotNull();
      softly.assertThat(currentBase.getStockAnchorPoints()).contains(insideLow);
    });
  }

  @Test
  @DisplayName("베이스 안쪽 고점이 들어오면 아직 베이스가 없는 점들이 베이스에 편입된다")
  void resolve_withHighInsideBase_assignsUnassignedPoints() {
    // given
    StockAnchorPoint insideHigh = savePoint(12_000L, TRADE_DATE, HIGH);

    // when
    stockBasePointIntegrator.resolve(insideHigh, currentBase);

    // then
    assertThat(insideHigh.getStockBase()).isNotNull();
  }


  @Test
  @DisplayName("저항선 위로 벗어난 저점은 베이스에 편입하지 않는다")
  void resolve_withLowAboveResistance_assignsNothing() {
    // given
    StockAnchorPoint outsideLow = savePoint(16_000L, TRADE_DATE, LOW);

    // when
    stockBasePointIntegrator.resolve(outsideLow, currentBase);

    // then
    assertThat(outsideLow.getStockBase()).isNull();
  }

  @Test
  @DisplayName("지지선 아래로 벗어난 고점은 베이스에 편입하지 않는다")
  void resolve_withHighBelowSupport_assignsNothing() {
    // given
    StockAnchorPoint outsideHigh = savePoint(9_000L, TRADE_DATE, HIGH);

    // when
    stockBasePointIntegrator.resolve(outsideHigh, currentBase);

    // then
    assertThat(outsideHigh.getStockBase()).isNull();
  }

  @Test
  @DisplayName("베이스보다 높은 고점이 편입되면 최고 저항선이 갱신된다")
  void resolve_withHigherHighInsideBase_updatesHighestResistance() {
    // given
    StockAnchorPoint higherHigh = savePoint(15_400L, TRADE_DATE, HIGH);

    // when
    stockBasePointIntegrator.resolve(higherHigh, currentBase);

    // then
    assertThat(currentBase.getHighestResistanceLine().getPrice()).isEqualTo(15_400L);
  }

  private StockAnchorPoint savePoint(long price, LocalDate tradeDate, StockAnchorPointType type) {
    return stockAnchorPointRepository.save(new StockAnchorPoint(price, VOLUME, tradeDate, type, null, stock));
  }
}
