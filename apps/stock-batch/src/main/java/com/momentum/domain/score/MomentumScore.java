package com.momentum.domain.score;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MomentumScore {

  private static final int SCORE_PRECISION = 38;
  private static final int SCORE_SCALE = 6;

  @Column(name = "momentum", precision = SCORE_PRECISION, scale = SCORE_SCALE)
  private BigDecimal value;

  private MomentumScore(BigDecimal value) {
    this.value = Objects.requireNonNull(value);
  }

  // 12개월 모멘텀 = (현재가 - 1년전가) / 1년전가
  public static MomentumScore calculate(long currentPrice, long pastPrice) {
    BigDecimal value = BigDecimal.valueOf(currentPrice)
        .subtract(BigDecimal.valueOf(pastPrice))
        .divide(BigDecimal.valueOf(pastPrice), 6, RoundingMode.HALF_UP);
    return new MomentumScore(value);
  }
}
