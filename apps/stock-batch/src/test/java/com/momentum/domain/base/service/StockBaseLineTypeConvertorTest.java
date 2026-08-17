package com.momentum.domain.base.service;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseLine;
import com.momentum.domain.base.entity.StockBaseLineType;
import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.sharedkernel.StockTrend;
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
class StockBaseLineTypeConvertorTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2026, 5, 20);
  private static final long VOLUME = 100_000L;

  @Autowired
  private StockBaseLineTypeConvertor stockBaseLineTypeConvertor;
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
  @DisplayName("새 베이스가 위로 올라서면 아래에 남은 저항선은 지지선이 된다")
  void convertLineType_whenNewBaseIsAbove_turnsResistanceIntoSupport() {
    // given
    StockBase previousBase = saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(20));
    StockBase newBase = saveBase(16_000L, 14_000L, TRADE_DATE);

    // when
    stockBaseLineTypeConvertor.convertLineType(previousBase, newBase);

    // then
    assertThat(typesOf(previousBase)).containsOnly(StockBaseLineType.SUPPORT);
  }

  @Test
  @DisplayName("새 베이스가 아래로 내려서면 위에 남은 지지선은 저항선이 된다")
  void convertLineType_whenNewBaseIsBelow_turnsSupportIntoResistance() {
    // given
    StockBase previousBase = saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(20));
    StockBase newBase = saveBase(8_000L, 7_000L, TRADE_DATE);

    // when
    stockBaseLineTypeConvertor.convertLineType(previousBase, newBase);

    // then
    assertThat(typesOf(previousBase)).containsOnly(StockBaseLineType.RESISTANCE);
  }

  @Test
  @DisplayName("새 베이스가 이전 베이스와 겹치면 선 종류를 바꾸지 않는다")
  void convertLineType_whenBasesOverlap_keepsLineTypes() {
    // given
    StockBase previousBase = saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(20));
    StockBase newBase = saveBase(11_500L, 10_500L, TRADE_DATE);

    // when
    stockBaseLineTypeConvertor.convertLineType(previousBase, newBase);

    // then
    assertThat(typesOf(previousBase))
        .containsExactlyInAnyOrder(StockBaseLineType.RESISTANCE, StockBaseLineType.SUPPORT);
  }

  @Test
  @DisplayName("새 베이스가 없으면 아무것도 바꾸지 않는다")
  void convertLineType_withoutNewBase_keepsLineTypes() {
    // given
    StockBase previousBase = saveBase(11_000L, 10_000L, TRADE_DATE.minusDays(20));

    // when
    stockBaseLineTypeConvertor.convertLineType(previousBase, null);

    // then
    assertThat(typesOf(previousBase))
        .containsExactlyInAnyOrder(StockBaseLineType.RESISTANCE, StockBaseLineType.SUPPORT);
  }

  private List<StockBaseLineType> typesOf(StockBase base) {
    return base.getStockBaseLines().stream().map(StockBaseLine::getType).toList();
  }

  private StockBase saveBase(long highPrice, long lowPrice, LocalDate tradeDate) {
    StockAnchorPoint high = savePoint(highPrice, tradeDate, HIGH);
    StockAnchorPoint low = savePoint(lowPrice, tradeDate.plusDays(1), LOW);
    return stockBaseRepository.save(StockBase.init(high, low, VOLUME));
  }

  private StockAnchorPoint savePoint(long price, LocalDate tradeDate, StockAnchorPointType type) {
    return stockAnchorPointRepository.save(new StockAnchorPoint(price, VOLUME, tradeDate, type, null, stock));
  }
}
