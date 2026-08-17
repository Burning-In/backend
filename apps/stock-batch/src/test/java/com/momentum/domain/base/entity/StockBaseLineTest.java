package com.momentum.domain.base.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockBaseLineTest {

  private final Stock stock = Stock.of("종목", "000040", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("고점이면 저항선(RESISTANCE)을 생성한다")
  void createResistanceFromPivotHigh() {
    long resistancePrice = 10_000L;
    long baseAverageVolume = 100_000L;
    StockAnchorPoint point = point(resistancePrice, StockAnchorPointType.HIGH);

    StockBaseLine line = StockBaseLine.create(point, baseAverageVolume, null);

    assertSoftly(softly -> {
      softly.assertThat(line.getType()).isEqualTo(StockBaseLineType.RESISTANCE);
      softly.assertThat(line.getPrice()).isEqualTo(resistancePrice);
    });
  }

  @Test
  @DisplayName("저점이면 지지선(SUPPORT)을 생성한다")
  void createSupportFromPivotLow() {
    long supportPrice = 8_000L;
    long baseAverageVolume = 100_000L;
    StockAnchorPoint point = point(supportPrice, StockAnchorPointType.LOW);

    StockBaseLine line = StockBaseLine.create(point, baseAverageVolume, null);

    assertSoftly(softly -> {
      softly.assertThat(line.getType()).isEqualTo(StockBaseLineType.SUPPORT);
      softly.assertThat(line.getPrice()).isEqualTo(supportPrice);
    });
  }

  @Test
  @DisplayName("저항선과 지지선은 서로 타입이 변경될 수 있다.")
  void convertLineType() {
    long resistancePrice = 10_000L;
    long averageVolume = 100L;
    StockBaseLine line = StockBaseLine.create(point(resistancePrice, StockAnchorPointType.HIGH), averageVolume, null);

    line.convertLineType();
    assertThat(line.getType()).isEqualTo(StockBaseLineType.SUPPORT);

    line.convertLineType();
    assertThat(line.getType()).isEqualTo(StockBaseLineType.RESISTANCE);
  }

  @Test
  @DisplayName("같은 타입이고 가격이 임계 범위 안인 점은 같은 라인으로 본다")
  void matchesWhenSameTypeAndWithinThreshold() {
    long resistancePrice = 10_000L;
    long averageVolume = 100L;
    long nearPrice = 10_200L;
    double matchThresholdPercent = 5.0;
    StockBaseLine line = StockBaseLine.create(point(resistancePrice, StockAnchorPointType.HIGH), averageVolume, null);
    StockAnchorPoint point = point(nearPrice, StockAnchorPointType.HIGH);

    assertThat(line.matches(point, matchThresholdPercent)).isTrue();
  }

  @Test
  @DisplayName("타입이 다른 점은 다른 라인으로 본다")
  void doesNotMatchWhenDifferentType() {
    long supportPrice = 10_000L;
    long averageVolume = 100L;
    long nearPrice = 10_200L;
    double matchThresholdPercent = 5.0;
    StockBaseLine line = StockBaseLine.create(point(supportPrice, StockAnchorPointType.LOW), averageVolume, null);
    StockAnchorPoint point = point(nearPrice, StockAnchorPointType.HIGH);

    assertThat(line.matches(point, matchThresholdPercent)).isFalse();
  }

  @Test
  @DisplayName("가격이 임계 범위를 벗어난 점은 다른 라인으로 본다")
  void doesNotMatchWhenOutsideThreshold() {
    long resistancePrice = 10_000L;
    long averageVolume = 100L;
    long farPrice = 12_000L;
    double matchThresholdPercent = 5.0;
    StockBaseLine line = StockBaseLine.create(point(resistancePrice, StockAnchorPointType.HIGH), averageVolume, null);
    StockAnchorPoint point = point(farPrice, StockAnchorPointType.HIGH);

    assertThat(line.matches(point, matchThresholdPercent)).isFalse();
  }

  @Test
  @DisplayName("터치가 누적된 라인이 그렇지 않은 라인보다 강하다")
  void isStrongerThan() {
    long resistancePrice = 10_000L;
    long averageVolume = 100L;
    long additionalVolume = 100L;
    StockBaseLine touched = StockBaseLine.create(point(resistancePrice, StockAnchorPointType.HIGH), averageVolume, null);
    StockBaseLine untouched = StockBaseLine.create(point(resistancePrice, StockAnchorPointType.HIGH), averageVolume, null);

    touched.updateStrength(additionalVolume, averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(touched.isStrongerThan(untouched)).isTrue();
      softly.assertThat(untouched.isStrongerThan(touched)).isFalse();
    });
  }

  private StockAnchorPoint point(long price, StockAnchorPointType type) {
    return new StockAnchorPoint(price, 100L, LocalDate.now(), type, null, stock);
  }
}
