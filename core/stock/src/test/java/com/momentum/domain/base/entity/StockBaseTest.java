package com.momentum.domain.base.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
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

    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);

    assertThat(base.getStageLevel()).isEqualTo(1L);
  }

  @Test
  @DisplayName("upper로 생성하면 stageLevel이 현재 단계 + 1이다")
  void upperIncrementsStageLevel() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long currentStageLevel = 2L;

    StockBase base = StockBase.upper(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice),
        currentStageLevel, averageVolume);

    assertThat(base.getStageLevel()).isEqualTo(3L);
  }

  @Test
  @DisplayName("lower로 생성하면 stageLevel이 현재 단계 - 1이다")
  void lowerDecrementsStageLevel() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long currentStageLevel = 2L;

    StockBase base = StockBase.lower(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice),
        currentStageLevel, averageVolume);

    assertThat(base.getStageLevel()).isEqualTo(1L);
  }

  @Test
  @DisplayName("lower로 생성해도 stageLevel은 0 밑으로 내려가지 않는다")
  void lowerNeverDropsBelowZero() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long lowestStageLevel = 0L;

    StockBase base = StockBase.lower(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice),
        lowestStageLevel, averageVolume);

    assertThat(base.getStageLevel()).isEqualTo(0L);
  }

  @Test
  @DisplayName("고점/저점 폭이 임계치(15%) 이상이면 횡보구간(BASE)으로 분류된다")
  void classifiedAsBaseWhenWidthIsWide() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;

    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);

    assertThat(base.getStockBaseKind()).isEqualTo(StockBaseKind.BASE);
  }

  @Test
  @DisplayName("고점/저점 폭이 임계치(15%) 미만이면 눌림(PULLBACK)으로 분류된다")
  void classifiedAsPullbackWhenWidthIsNarrow() {
    long pullbackHighPrice = 11_400L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;

    StockBase base = StockBase.init(highAnchorPoint(pullbackHighPrice), lowAnchorPoint(lowPrice), averageVolume);

    assertThat(base.getStockBaseKind()).isEqualTo(StockBaseKind.PULLBACK);
  }

  @Test
  @DisplayName("고점/저점 한 쌍으로 생성 직후 base가 생성된다")
  void assignsBaseToAnchorPoints() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    StockAnchorPoint high = highAnchorPoint(highPrice);
    StockAnchorPoint low = lowAnchorPoint(lowPrice);

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
    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);
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
    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);

    base.update(updatedStageLevel, null);

    assertThat(base.getStageLevel()).isEqualTo(updatedStageLevel);
  }

  @Test
  @DisplayName("고점은 저항 상단을 넘어야 위로 벗어난 것으로 본다")
  void isAboveWithHighPoint() {
    StockBase base = base();

    assertSoftly(softly -> {
      softly.assertThat(base.isAbove(highAnchorPoint(12_200L))).isTrue();
      softly.assertThat(base.isAbove(highAnchorPoint(12_100L))).isFalse();
    });
  }

  @Test
  @DisplayName("저점은 저항 하단까지만 올라와도 위로 벗어난 것으로 본다")
  void isAboveWithLowPoint() {
    StockBase base = base();

    assertSoftly(softly -> {
      softly.assertThat(base.isAbove(lowAnchorPoint(11_800L))).isTrue();
      softly.assertThat(base.isAbove(lowAnchorPoint(11_700L))).isFalse();
    });
  }

  @Test
  @DisplayName("저점은 지지 하단을 뚫어야 아래로 벗어난 것으로 본다")
  void isBelowWithLowPoint() {
    StockBase base = base();

    assertSoftly(softly -> {
      softly.assertThat(base.isBelow(lowAnchorPoint(9_799L))).isTrue();
      softly.assertThat(base.isBelow(lowAnchorPoint(9_800L))).isFalse();
    });
  }

  @Test
  @DisplayName("고점은 지지 상단까지만 내려와도 아래로 벗어난 것으로 본다")
  void isBelowWithHighPoint() {
    StockBase base = base();

    assertSoftly(softly -> {
      softly.assertThat(base.isBelow(highAnchorPoint(10_199L))).isTrue();
      softly.assertThat(base.isBelow(highAnchorPoint(10_200L))).isFalse();
    });
  }

  @Test
  @DisplayName("위로도 아래로도 벗어나지 않은 점은 베이스 안에 있다")
  void containsWhenNeitherAboveNorBelow() {
    StockBase base = base();

    assertSoftly(softly -> {
      softly.assertThat(base.contains(lowAnchorPoint(11_000L))).isTrue();
      softly.assertThat(base.contains(highAnchorPoint(11_000L))).isTrue();
      softly.assertThat(base.contains(lowAnchorPoint(11_800L))).isFalse();
      softly.assertThat(base.contains(highAnchorPoint(10_199L))).isFalse();
    });
  }


  @Test
  @DisplayName("지지선에 닿은 저점도 베이스 안에 있는 것으로 본다")
  void containsLowPointTouchingSupport() {
    StockBase base = base();

    assertThat(base.contains(lowAnchorPoint(10_000L))).isTrue();
  }

  private StockBase base() {
    return StockBase.init(highAnchorPoint(12_000L), lowAnchorPoint(10_000L), 100_000L);
  }

  @Test
  @DisplayName("더 높은 고점을 통합하면 최고 저항선이 갱신된다")
  void integratePointsUpdatesHighestResistance() {
    long highPrice = 12_000L;
    long lowPrice = 10_000L;
    long averageVolume = 100_000L;
    long higherResistancePrice = 13_000L;
    double priceThreshold = 0.01;
    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);

    base.integratePoints(List.of(highAnchorPoint(higherResistancePrice)), averageVolume, priceThreshold);

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
    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);

    base.integratePoints(List.of(lowAnchorPoint(lowerSupportPrice)), averageVolume, priceThreshold);

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
    StockBase base = StockBase.init(highAnchorPoint(highPrice), lowAnchorPoint(lowPrice), averageVolume);
    // 생성 직후 최강 저항선은 초기 고점(12_000), 아직 터치가 없어 강도는 0이다
    assertThat(base.getStrongestResistanceLine().getPrice()).isEqualTo(highPrice);

    // 같은 가격(13_000) 고점 2개 통합 → 두 번째에서 같은 라인에 터치가 쌓여 강도가 초기 라인(0)을 넘어선다
    base.integratePoints(
        List.of(highAnchorPoint(strongerResistancePrice), highAnchorPoint(strongerResistancePrice)),
        averageVolume, priceThreshold);

    assertThat(base.getStrongestResistanceLine().getPrice()).isEqualTo(strongerResistancePrice);
  }

  private StockAnchorPoint highAnchorPoint(long price) {
    return new StockAnchorPoint(price, 100_000L, LocalDate.now(), StockAnchorPointType.HIGH, null, stock);
  }

  private StockAnchorPoint lowAnchorPoint(long price) {
    return new StockAnchorPoint(price, 100_000L, LocalDate.now(), StockAnchorPointType.LOW, null, stock);
  }
}
