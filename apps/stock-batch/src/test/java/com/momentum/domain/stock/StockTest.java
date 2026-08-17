package com.momentum.domain.stock;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockTest {

  @Test
  @DisplayName("다른 레짐으로 update하면 레짐이 바뀐다")
  void updatesRegimeWhenDifferent() {
    Stock stock = Stock.of("종목", "000040", BREAKOUT_READY, StockTrend.UPTREND);

    stock.update(BREAKOUT_SUCCESS);

    assertThat(stock.getStockRegime()).isEqualTo(BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("UNKNOWN으로 update하면 기존 레짐을 유지한다")
  void keepsRegimeWhenUnknown() {
    Stock stock = Stock.of("종목", "000040", BREAKOUT_READY, StockTrend.UPTREND);

    stock.update(UNKNOWN);

    assertThat(stock.getStockRegime()).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("null로 update하면 기존 레짐을 유지한다")
  void keepsRegimeWhenNull() {
    Stock stock = Stock.of("종목", "000040", BREAKOUT_READY, StockTrend.UPTREND);

    stock.update(null);

    assertThat(stock.getStockRegime()).isEqualTo(BREAKOUT_READY);
  }
}
