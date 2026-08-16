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
class StockBaseInitializerTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2026, 5, 20);
  private static final long VOLUME = 100_000L;

  @Autowired
  private StockBaseInitializer stockBaseInitializer;
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
  @DisplayName("짝이 될 반대 타입 점이 없으면 베이스를 만들지 않는다")
  void resolve_withoutPairedPoint_createsNothing() {
    // given
    StockAnchorPoint confirmedLow = savePoint(10_000L, TRADE_DATE, LOW);

    // when
    stockBaseInitializer.resolve(confirmedLow);

    // then
    assertThat(stockBaseRepository.findAllByStockOrderByStartedAt(stock)).isEmpty();
  }

  @Test
  @DisplayName("저점이 확정되면 직전 고점과 짝지어 첫 베이스를 만든다")
  void resolve_withConfirmedLow_createsFirstBase() {
    // given
    savePoint(11_000L, TRADE_DATE.minusDays(5), HIGH);
    StockAnchorPoint confirmedLow = savePoint(10_000L, TRADE_DATE, LOW);

    // when
    stockBaseInitializer.resolve(confirmedLow);

    // then
    StockBase created = onlyBase();
    assertSoftly(softly -> {
      softly.assertThat(created.getStageLevel()).isEqualTo(1L);
      softly.assertThat(created.getHighestResistanceLine().getPrice()).isEqualTo(11_000L);
      softly.assertThat(created.getLowestSupportLine().getPrice()).isEqualTo(10_000L);
    });
  }

  @Test
  @DisplayName("고점이 확정되어도 고가와 저가가 뒤바뀌지 않는다")
  void resolve_withConfirmedHigh_keepsHighAndLowInPlace() {
    // given
    savePoint(10_000L, TRADE_DATE.minusDays(5), LOW);
    StockAnchorPoint confirmedHigh = savePoint(11_000L, TRADE_DATE, HIGH);

    // when
    stockBaseInitializer.resolve(confirmedHigh);

    // then
    StockBase created = onlyBase();
    assertSoftly(softly -> {
      softly.assertThat(created.getHighestResistanceLine().getPrice()).isEqualTo(11_000L);
      softly.assertThat(created.getHighestResistanceLine().getType()).isEqualTo(StockBaseLineType.RESISTANCE);
      softly.assertThat(created.getLowestSupportLine().getPrice()).isEqualTo(10_000L);
      softly.assertThat(created.getLowestSupportLine().getType()).isEqualTo(StockBaseLineType.SUPPORT);
    });
  }

  @Test
  @DisplayName("다른 종목의 점은 짝으로 쓰지 않는다")
  void resolve_withPairedPointOfOtherStock_createsNothing() {
    // given
    Stock otherStock = stockRepository.save(Stock.of("하이닉스", "000660", StockRegime.UNKNOWN, StockTrend.UPTREND));
    stockAnchorPointRepository.save(
        new StockAnchorPoint(11_000L, VOLUME, TRADE_DATE.minusDays(5), HIGH, null, otherStock));
    StockAnchorPoint confirmedLow = savePoint(10_000L, TRADE_DATE, LOW);

    // when
    stockBaseInitializer.resolve(confirmedLow);

    // then
    assertThat(stockBaseRepository.findAllByStockOrderByStartedAt(stock)).isEmpty();
  }

  @Test
  @DisplayName("이미 베이스에 속한 점은 짝으로 쓰지 않는다")
  void resolve_withPairedPointAlreadyAssigned_createsNothing() {
    // given
    StockAnchorPoint assignedHigh = savePoint(11_000L, TRADE_DATE.minusDays(10), HIGH);
    StockAnchorPoint assignedLow = savePoint(10_000L, TRADE_DATE.minusDays(9), LOW);
    stockBaseRepository.save(StockBase.init(assignedHigh, assignedLow, VOLUME));
    StockAnchorPoint confirmedLow = savePoint(9_000L, TRADE_DATE, LOW);

    // when
    stockBaseInitializer.resolve(confirmedLow);

    // then
    assertThat(stockBaseRepository.findAllByStockOrderByStartedAt(stock)).hasSize(1);
  }

  private StockBase onlyBase() {
    List<StockBase> bases = stockBaseRepository.findAllByStockOrderByStartedAt(stock);
    assertThat(bases).hasSize(1);
    return bases.getFirst();
  }

  private StockAnchorPoint savePoint(long price, LocalDate tradeDate, StockAnchorPointType type) {
    return stockAnchorPointRepository.save(new StockAnchorPoint(price, VOLUME, tradeDate, type, null, stock));
  }
}
