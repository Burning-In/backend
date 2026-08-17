package com.momentum.domain.relativestrength;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.stock.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KospiRelativeStrength extends BaseEntity {

  private static final int RATING_SPAN = 98;
  private static final int MIN_RATING = 1;

  private int rsScore;

  @ManyToOne
  private Stock stock;
  @ManyToOne
  private Kospi kospi;

  private KospiRelativeStrength(int rsScore, Stock stock, Kospi kospi) {
    this.rsScore = rsScore;
    this.stock = stock;
    this.kospi = kospi;
  }

  public static KospiRelativeStrength of(int rsScore, Stock stock, Kospi kospi) {
    return new KospiRelativeStrength(rsScore, stock, kospi);
  }

  // rank는 원점수 오름차순 정렬 상의 순위(0부터 시작)여야 한다.
  public static KospiRelativeStrength create(int rank, int totalCount, Stock stock, Kospi kospi) {
    double percentile = (double) rank / totalCount;
    int rsScore = (int) Math.round(percentile * RATING_SPAN) + MIN_RATING;
    return new KospiRelativeStrength(rsScore, stock, kospi);
  }
}
