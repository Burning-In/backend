package com.momentum.domain.anchorpoint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.anchorpoint.service.typedecider.StockAnchorPointTypeDecider;
import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.sharedkernel.StockTrend;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class StockAnchorPointTypeDeciderTest {

  @Autowired
  private StockAnchorPointTypeDecider stockAnchorPointTypeDecider;
  @Autowired
  private StockAnchorPointRepository stockAnchorPointRepository;
  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.OTHER));
  }

  @Test
  @DisplayName("점이 3개 미만이면 타입을 정할 수 없어 예외가 발생한다")
  void resolvePointTypes_withLessThanThreePoints_throwsException() {
    // given
    savePoint("2024-01-01", 10_000L);
    savePoint("2024-01-02", 11_000L);

    // when & then
    assertThatThrownBy(() -> stockAnchorPointTypeDecider.resolvePointTypes(stock))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("직전 점과 가격이 다르면 직전 점을 이웃으로 삼아 대상 점 하나만 타입이 정해진다")
  void resolvePointTypes_withDifferentPriceFromPrevious_updatesTargetOnly() {
    // given
    savePoint("2024-01-01", 10_000L);
    StockAnchorPoint previous = savePoint("2024-01-02", 10_000L);
    StockAnchorPoint target = savePoint("2024-01-03", 12_000L);
    savePoint("2024-01-04", 11_000L);

    // when
    List<StockAnchorPoint> resolved = stockAnchorPointTypeDecider.resolvePointTypes(stock);

    // then
    assertSoftly(softly -> {
      softly.assertThat(resolved).hasSize(1);
      softly.assertThat(target.getType()).isEqualTo(StockAnchorPointType.HIGH);
      softly.assertThat(previous.getType()).isEqualTo(StockAnchorPointType.UNKNOWN);
    });
  }

  @Test
  @DisplayName("직전 점과 가격이 같으면 한 쌍으로 묶여 두 점이 같은 타입으로 정해진다")
  void resolvePointTypes_withSamePriceAsPrevious_updatesBothAsPair() {
    // given
    savePoint("2024-01-01", 10_000L);
    StockAnchorPoint previous = savePoint("2024-01-02", 12_000L);
    StockAnchorPoint target = savePoint("2024-01-03", 12_000L);
    savePoint("2024-01-04", 11_000L);

    // when
    List<StockAnchorPoint> resolved = stockAnchorPointTypeDecider.resolvePointTypes(stock);

    // then
    assertSoftly(softly -> {
      softly.assertThat(resolved).hasSize(2);
      softly.assertThat(target.getType()).isEqualTo(StockAnchorPointType.HIGH);
      softly.assertThat(previous.getType()).isEqualTo(StockAnchorPointType.HIGH);
    });
  }

  @Test
  @DisplayName("가격 차이가 1퍼센트 이하면 같은 가격으로 보고 한 쌍으로 묶는다")
  void resolvePointTypes_withinSamePriceThreshold_updatesBothAsPair() {
    // given
    savePoint("2024-01-01", 10_000L);
    savePoint("2024-01-02", 12_000L);
    savePoint("2024-01-03", 12_100L);
    savePoint("2024-01-04", 11_000L);

    // when
    List<StockAnchorPoint> resolved = stockAnchorPointTypeDecider.resolvePointTypes(stock);

    // then
    assertThat(resolved).hasSize(2);
  }

  @Test
  @DisplayName("점이 3개뿐이라 직전 점이 없으면 직후 점만으로 판정한다")
  void resolvePointTypes_withoutOldestPoint_classifiesWithNextOnly() {
    // given
    savePoint("2024-01-02", 12_000L);
    StockAnchorPoint target = savePoint("2024-01-03", 12_000L);
    savePoint("2024-01-04", 11_000L);

    // when
    List<StockAnchorPoint> resolved = stockAnchorPointTypeDecider.resolvePointTypes(stock);

    // then
    assertSoftly(softly -> {
      softly.assertThat(resolved).hasSize(2);
      softly.assertThat(target.getType()).isEqualTo(StockAnchorPointType.HIGH);
    });
  }

  private StockAnchorPoint savePoint(String tradeDate, long price) {
    return stockAnchorPointRepository.save(
        StockAnchorPoint.create(price, 1_000L, LocalDate.parse(tradeDate), stock));
  }
}
