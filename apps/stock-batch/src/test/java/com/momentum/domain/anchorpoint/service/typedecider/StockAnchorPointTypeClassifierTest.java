package com.momentum.domain.anchorpoint.service.typedecider;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.ASCENDING;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.DESCENDING;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.FLAT;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockAnchorPointTypeClassifierTest {

  private final StockAnchorPointTypeClassifier classifier = new StockAnchorPointTypeClassifier();

  @Test
  @DisplayName("직전 값이 없으면 직후와의 대소만으로 판정한다")
  void classify_withoutPreviousPrice_comparesWithNextOnly() {
    assertSoftly(softly -> {
      softly.assertThat(classifier.classify(null, 100L, 90L)).isEqualTo(HIGH);
      softly.assertThat(classifier.classify(null, 90L, 100L)).isEqualTo(LOW);
      softly.assertThat(classifier.classify(null, 100L, 100L)).isEqualTo(FLAT);
    });
  }

  @Test
  @DisplayName("직전·직후보다 모두 높으면 HIGH, 모두 낮으면 LOW")
  void classify_higherOrLowerThanBothNeighbors_returnsHighOrLow() {
    assertSoftly(softly -> {
      softly.assertThat(classifier.classify(80L, 120L, 90L)).isEqualTo(HIGH);
      softly.assertThat(classifier.classify(120L, 80L, 100L)).isEqualTo(LOW);
    });
  }

  @Test
  @DisplayName("한쪽으로 계속 오르거나 내리면 ASCENDING 또는 DESCENDING")
  void classify_betweenNeighbors_returnsAscendingOrDescending() {
    assertSoftly(softly -> {
      softly.assertThat(classifier.classify(80L, 90L, 100L)).isEqualTo(ASCENDING);
      softly.assertThat(classifier.classify(100L, 90L, 80L)).isEqualTo(DESCENDING);
    });
  }

  @Test
  @DisplayName("이웃과 값이 같아 어느 쪽도 아니면 FLAT")
  void classify_tiedWithNeighbor_returnsFlat() {
    assertSoftly(softly -> {
      softly.assertThat(classifier.classify(80L, 100L, 100L)).isEqualTo(FLAT);
      softly.assertThat(classifier.classify(100L, 100L, 80L)).isEqualTo(FLAT);
    });
  }
}
