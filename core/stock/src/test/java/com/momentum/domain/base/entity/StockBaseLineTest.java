package com.momentum.domain.base.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockBaseLineTest {

  private final Stock stock = new Stock("종목", "000001", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("고점이면 저항선(RESISTANCE)을 생성한다")
  void createResistanceFromPivotHigh() {
    long resistancePrice = 10_000L;
    long baseAverageVolume = 100_000L;
    StockPricePoint point = point(resistancePrice, StockPricePointType.PIVOT_HIGH);

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
    StockPricePoint point = point(supportPrice, StockPricePointType.PIVOT_LOW);

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
    StockBaseLine line = StockBaseLine.create(point(resistancePrice, StockPricePointType.PIVOT_HIGH), averageVolume, null);

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
    double matchThreshold = 0.05;
    StockBaseLine line = StockBaseLine.create(point(resistancePrice, StockPricePointType.PIVOT_HIGH), averageVolume, null);
    StockPricePoint point = point(nearPrice, StockPricePointType.PIVOT_HIGH);

    assertThat(line.matches(point, matchThreshold)).isTrue();
  }

  @Test
  @DisplayName("타입이 다른 점은 다른 라인으로 본다")
  void doesNotMatchWhenDifferentType() {
    long supportPrice = 10_000L;
    long averageVolume = 100L;
    long nearPrice = 10_200L;
    double matchThreshold = 0.05;
    StockBaseLine line = StockBaseLine.create(point(supportPrice, StockPricePointType.PIVOT_LOW), averageVolume, null);
    StockPricePoint point = point(nearPrice, StockPricePointType.PIVOT_HIGH);

    assertThat(line.matches(point, matchThreshold)).isFalse();
  }

  @Test
  @DisplayName("가격이 임계 범위를 벗어난 점은 다른 라인으로 본다")
  void doesNotMatchWhenOutsideThreshold() {
    long resistancePrice = 10_000L;
    long averageVolume = 100L;
    long farPrice = 12_000L;
    double matchThreshold = 0.05;
    StockBaseLine line = StockBaseLine.create(point(resistancePrice, StockPricePointType.PIVOT_HIGH), averageVolume, null);
    StockPricePoint point = point(farPrice, StockPricePointType.PIVOT_HIGH);

    assertThat(line.matches(point, matchThreshold)).isFalse();
  }

  @Test
  @DisplayName("터치가 누적된 라인이 그렇지 않은 라인보다 강하다")
  void isStrongerThan() {
    long resistancePrice = 10_000L;
    long averageVolume = 100L;
    long additionalVolume = 100L;
    StockBaseLine touched = StockBaseLine.create(point(resistancePrice, StockPricePointType.PIVOT_HIGH), averageVolume, null);
    StockBaseLine untouched = StockBaseLine.create(point(resistancePrice, StockPricePointType.PIVOT_HIGH), averageVolume, null);

    touched.updateStrength(additionalVolume, averageVolume);

    assertSoftly(softly -> {
      softly.assertThat(touched.isStrongerThan(untouched)).isTrue();
      softly.assertThat(untouched.isStrongerThan(touched)).isFalse();
    });
  }

  private StockPricePoint point(long price, StockPricePointType type) {
    return new StockPricePoint(price, 100L, LocalDate.now(), type, null, stock);
  }
}
