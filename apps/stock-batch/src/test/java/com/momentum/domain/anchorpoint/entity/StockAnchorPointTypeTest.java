package com.momentum.domain.anchorpoint.entity;

import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.ASCENDING;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.DESCENDING;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.FLAT;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.HIGH;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.LOW;
import static com.momentum.domain.anchorpoint.entity.StockAnchorPointType.UNKNOWN;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockAnchorPointTypeTest {

  @Test
  @DisplayName("HIGH와 LOW만 특이점이다")
  void isNonPivot_onlyHighAndLowArePivots() {
    assertSoftly(softly -> {
      softly.assertThat(HIGH.isNonPivot()).isFalse();
      softly.assertThat(LOW.isNonPivot()).isFalse();
      softly.assertThat(FLAT.isNonPivot()).isTrue();
      softly.assertThat(ASCENDING.isNonPivot()).isTrue();
      softly.assertThat(DESCENDING.isNonPivot()).isTrue();
      softly.assertThat(UNKNOWN.isNonPivot()).isTrue();
    });
  }
}
