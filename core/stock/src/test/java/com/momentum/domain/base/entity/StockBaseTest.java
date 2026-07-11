package com.momentum.domain.base.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockBaseTest {

  private final Stock stock = Stock.of("종목", "000040", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("init로 생성하면 stageLevel은 1이다")
  void initHasStageLevelOne() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;

    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    assertThat(base.getStageLevel()).isEqualTo(1L);
  }

  @Test
  @DisplayName("upper로 생성하면 stageLevel이 현재 단계 + 1이다")
  void upperIncrementsStageLevel() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long currentStageLevel = 2L;

    StockBase base = StockBase.upper(highPricePoint(highPrice), lowPricePoint(lowPrice),
        currentStageLevel, averageVolume);

    assertThat(base.getStageLevel()).isEqualTo(3L);
  }

  @Test
  @DisplayName("고점/저점 변동성이 임계치(10%) 이상이면 BASE로 분류된다")
  void classifiedAsBaseWhenVolatilityHigh() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;

    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    assertThat(base.getStockBaseKind()).isEqualTo(StockBaseKind.BASE);
  }

  @Test
  @DisplayName("고점/저점 변동성이 임계치(10%) 미만이면 PULLBACK으로 분류된다")
  void classifiedAsPullbackWhenVolatilityLow() {
    long pullbackHighPrice = 10_500L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;

    StockBase base = StockBase.init(highPricePoint(pullbackHighPrice), lowPricePoint(lowPrice), averageVolume);

    assertThat(base.getStockBaseKind()).isEqualTo(StockBaseKind.PULLBACK);
  }

  @Test
  @DisplayName("고점/저점 한 쌍으로 생성 직후 base가 생성된다")
  void assignsBaseToPricePoints() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    StockPricePoint high = highPricePoint(highPrice);
    StockPricePoint low = lowPricePoint(lowPrice);

    StockBase base = StockBase.init(high, low, averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(high.getStockBase()).isSameAs(base);
      softly.assertThat(low.getStockBase()).isSameAs(base);
    });
  }

  @Test
  @DisplayName("변동성 이력을 입력하면 VCP 여부를 판단해준다")
  void updateMarksVcp() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    List<Long> shrinkingVolatility = List.of(100L, 50L);
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);
    assertThat(base.isVcp()).isFalse();

    base.update(null, shrinkingVolatility);
    assertThat(base.isVcp()).isTrue();
  }

  @Test
  @DisplayName("기존 베이스의 stageLevel을 갱신할 수 있다")
  void updateChangesStageLevel() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long updatedStageLevel = 5L;
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    base.update(updatedStageLevel, null);

    assertThat(base.getStageLevel()).isEqualTo(updatedStageLevel);
  }

  @Test
  @DisplayName("저항선 상단/하단 경계는 최고 저항선 가격에 임계치를 적용한 값이다")
  void resistanceBounds() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    double boundThreshold = 5.0;
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(base.getResistanceUpperBound(boundThreshold)).isEqualTo(12_600L);
      softly.assertThat(base.getResistanceLowerBound(boundThreshold)).isEqualTo(11_400L);
    });
  }

  @Test
  @DisplayName("지지선 상단/하단 경계는 최저 지지선 가격에 임계치를 적용한 값이다")
  void supportBounds() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    double boundThreshold = 5.0;
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(base.getSupportUpperBound(boundThreshold)).isEqualTo(10_500L);
      softly.assertThat(base.getSupportLowerBound(boundThreshold)).isEqualTo(9_500L);
    });
  }

  @Test
  @DisplayName("더 높은 고점을 통합하면 최고 저항선이 갱신된다")
  void integratePointsUpdatesHighestResistance() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long higherResistancePrice = 13_000L;
    double priceThreshold = 0.01;
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    base.integratePoints(List.of(highPricePoint(higherResistancePrice)), averageVolume, priceThreshold);

    assertThat(base.getHighestResistanceLine().getPrice()).isEqualTo(higherResistancePrice);
  }

  @Test
  @DisplayName("더 낮은 저점을 통합하면 최저 지지선이 갱신된다")
  void integratePointsUpdatesLowestSupport() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long lowerSupportPrice = 9_000L;
    double priceThreshold = 0.01;
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);

    base.integratePoints(List.of(lowPricePoint(lowerSupportPrice)), averageVolume, priceThreshold);

    assertThat(base.getLowestSupportLine().getPrice()).isEqualTo(lowerSupportPrice);
  }

  @Test
  @DisplayName("새로 통합된 저항선이 기존 최강 저항선보다 강해지면 최강 저항선이 갱신된다")
  void integratePointsUpdatesStrongestResistanceLine() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long strongerResistancePrice = 13_000L;
    double priceThreshold = 0.01;
    StockBase base = StockBase.init(highPricePoint(highPrice), lowPricePoint(lowPrice), averageVolume);
    // 생성 직후 최강 저항선은 초기 고점(12_000), 아직 터치가 없어 강도는 0이다
    assertThat(base.getStrongestResistanceLine().getPrice()).isEqualTo(highPrice);

    // 같은 가격(13_000) 고점 2개 통합 → 두 번째에서 같은 라인에 터치가 쌓여 강도가 초기 라인(0)을 넘어선다
    base.integratePoints(
        List.of(highPricePoint(strongerResistancePrice), highPricePoint(strongerResistancePrice)),
        averageVolume, priceThreshold);

    assertThat(base.getStrongestResistanceLine().getPrice()).isEqualTo(strongerResistancePrice);
  }

  private StockPricePoint highPricePoint(long price) {
    return new StockPricePoint(price, 100_000L, LocalDate.now(), StockPricePointType.HIGH, null, stock);
  }

  private StockPricePoint lowPricePoint(long price) {
    return new StockPricePoint(price, 100_000L, LocalDate.now(), StockPricePointType.LOW, null, stock);
  }
}
