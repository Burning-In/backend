package com.momentum.domain.stock;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.domain.stock.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockTest {

  @Test
  @DisplayName("레짐이 바뀌면 StockStateChangedEvent를 도메인 이벤트로 등록한다")
  void registersEventWhenRegimeChanges() {
    Stock stock = Stock.of("종목", "000040", BREAKOUT_READY, StockTrend.UPTREND);

    stock.update(BREAKOUT_SUCCESS);

    assertThat(stock.domainEvents())
        .containsExactly(new StockStateChangedEvent("000040", BREAKOUT_READY, BREAKOUT_SUCCESS));
  }

  @Test
  @DisplayName("같은 레짐으로 update하면 이벤트를 등록하지 않는다")
  void noEventWhenSameRegime() {
    Stock stock = Stock.of("종목", "000040", BREAKOUT_READY, StockTrend.UPTREND);

    stock.update(BREAKOUT_READY);

    assertThat(stock.domainEvents()).isEmpty();
  }

  @Test
  @DisplayName("UNKNOWN으로 update하면 기존 레짐을 유지하고 이벤트도 등록하지 않는다")
  void noEventWhenUnknown() {
    Stock stock = Stock.of("종목", "000040", BREAKOUT_READY, StockTrend.UPTREND);

    stock.update(UNKNOWN);

    assertThat(stock.getStockRegime()).isEqualTo(BREAKOUT_READY);
    assertThat(stock.domainEvents()).isEmpty();
  }
}
