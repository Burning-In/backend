package com.momentum.domain.score;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockRankScoreTest {

  private final Stock stock = new Stock("종목", "000001", StockRegime.BREAKOUT_READY, StockTrend.UPTREND);

  @Test
  @DisplayName("종가들을 받아 모멘텀과 FIP를 계산한다.")
  void createFromClosePrices() {
    LocalDate baseDate = LocalDate.of(2024, 1, 15);
    List<Long> closePrices = List.of(12_000L, 11_000L, 10_000L);

    StockRankScore score = StockRankScore.create(closePrices, baseDate, stock);

    assertSoftly(softly -> {
      softly.assertThat(score.getMomentumScore().getValue()).isEqualByComparingTo(new BigDecimal("0.2"));
      softly.assertThat(score.getFrogInPanScore()).isNotNull();
      softly.assertThat(score.getBaseDate()).isEqualTo(baseDate);
      softly.assertThat(score.getStock()).isSameAs(stock);
    });
  }
}
