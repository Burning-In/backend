package com.momentum.domain.relativestrength;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class KospiRelativeStrengthTest {

  private static final int MIN_RATING = 1;
  private static final int MAX_RATING = 99;

  // 등급은 순위에 대해 단조 증가하므로, 최하위/최상위만 확인하면 나머지 순위는 자연히 범위 안에 들어온다.
  @ParameterizedTest(name = "종목 {0}개")
  @ValueSource(ints = {1, 2, 3, 10, 99, 100, 101, 1_000, 2_500})
  @DisplayName("종목 수가 몇 개든 RS 등급은 1~99를 벗어나지 않는다")
  void create_neverLeavesRatingBounds(int totalCount) {
    assertThat(rsScoreOf(0, totalCount)).isBetween(MIN_RATING, MAX_RATING);
    assertThat(rsScoreOf(totalCount - 1, totalCount)).isBetween(MIN_RATING, MAX_RATING);
  }

  @ParameterizedTest(name = "종목 {0}개")
  @ValueSource(ints = {1, 2, 3, 10, 99, 100, 101, 1_000, 2_500})
  @DisplayName("원점수가 가장 낮은 종목은 종목 수와 무관하게 항상 1등급이다")
  void create_withLowestRank_alwaysReturnsMinRating(int totalCount) {
    assertThat(rsScoreOf(0, totalCount)).isEqualTo(MIN_RATING);
  }

  // 최상위 등급은 (종목수-1)/종목수 의 백분위이므로 종목이 많아질수록 99에 근접하되 넘지는 않는다.
  @ParameterizedTest(name = "종목 {0}개 → 최상위 {1}등급")
  @CsvSource({
      "1, 1",
      "2, 50",
      "3, 66",
      "100, 98",
      "1000, 99",
      "2500, 99"
  })
  @DisplayName("최상위 종목의 등급은 종목 수가 아무리 많아도 99에서 멈춘다")
  void create_withHighestRank_capsAtMaxRating(int totalCount, int expectedRsScore) {
    assertThat(rsScoreOf(totalCount - 1, totalCount)).isEqualTo(expectedRsScore);
  }

  // 등급 계산은 stock, kospi를 참조하지 않으므로 연관 엔티티는 비워 둔다.
  private int rsScoreOf(int rank, int totalCount) {
    return KospiRelativeStrength.create(rank, totalCount, null, null).getRsScore();
  }
}
