package com.momentum.domain;

import static com.momentum.domain.SnapshotJudgment.BUY;
import static com.momentum.domain.SnapshotJudgment.SELL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockSnapShotTest {

  private static final LocalDateTime RECORDED_AT = LocalDateTime.of(2026, 5, 10, 9, 30);

  @Test
  @DisplayName("create는 생성 시점의 종목 레짐·가격을 박제한다")
  void captureFreezesRegimeAndPrice() {
    Stock stock = stock(StockRegime.BREAKOUT_READY);

    StockSnapShot snapshot = StockSnapShot.create(stock, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

    assertThat(snapshot.getCapturedRegime()).isEqualTo(StockRegime.BREAKOUT_READY);
    assertThat(snapshot.getCapturedPrice()).isEqualTo(10_000L);
    assertThat(snapshot.getJudgment()).isEqualTo(BUY);
    assertThat(snapshot.getRecordedAt()).isEqualTo(RECORDED_AT);
    assertThat(snapshot.getRetrospective()).isEqualTo("회고");
  }

  @Test
  @DisplayName("박제 후 종목 레짐이 바뀌어도 스냅샷의 레짐은 그대로다")
  void capturedRegimeIsImmutableToLaterStockChange() {
    Stock stock = stock(StockRegime.BREAKOUT_READY);
    StockSnapShot snapshot = StockSnapShot.create(stock, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

    stock.update(StockRegime.BREAKOUT_SUCCESS);

    assertThat(snapshot.getCapturedRegime()).isEqualTo(StockRegime.BREAKOUT_READY);
  }

  @Test
  @DisplayName("create는 참고 스냅샷을 SnapshotReference 링크로 연결한다")
  void createLinksReferences() {
    Stock stock = stock(StockRegime.BREAKOUT_READY);
    StockSnapShot ref1 = StockSnapShot.create(stock, 9_000L, BUY, List.of(), RECORDED_AT, "ref1");
    StockSnapShot ref2 = StockSnapShot.create(stock, 9_500L, BUY, List.of(), RECORDED_AT, "ref2");

    StockSnapShot snapshot = StockSnapShot.create(stock, 10_000L, BUY, List.of(ref1, ref2), RECORDED_AT, "회고");

    assertThat(snapshot.getReferences())
        .extracting(SnapshotReference::getReferenced)
        .containsExactly(ref1, ref2);
  }

  @Test
  @DisplayName("update는 판단·참고·회고만 바꾸고 박제값(레짐·가격·기록시점)은 유지한다")
  void updateKeepsCapturedValues() {
    Stock stock = stock(StockRegime.BREAKOUT_READY);
    StockSnapShot ref = StockSnapShot.create(stock, 9_000L, BUY, List.of(), RECORDED_AT, "ref");
    StockSnapShot snapshot = StockSnapShot.create(stock, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

    snapshot.update(SELL, List.of(ref), "수정된 회고");

    assertThat(snapshot.getJudgment()).isEqualTo(SELL);
    assertThat(snapshot.getRetrospective()).isEqualTo("수정된 회고");
    assertThat(snapshot.getReferences())
        .extracting(SnapshotReference::getReferenced)
        .containsExactly(ref);
    assertThat(snapshot.getCapturedRegime()).isEqualTo(StockRegime.BREAKOUT_READY);
    assertThat(snapshot.getCapturedPrice()).isEqualTo(10_000L);
    assertThat(snapshot.getRecordedAt()).isEqualTo(RECORDED_AT);
  }

  @Test
  @DisplayName("자기 자신을 참고 스냅샷으로 추가하면 예외를 던진다")
  void rejectsSelfReference() {
    Stock stock = stock(StockRegime.BREAKOUT_READY);
    StockSnapShot snapshot = StockSnapShot.create(stock, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

    assertThatThrownBy(() -> snapshot.update(BUY, List.of(snapshot), "회고"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private Stock stock(StockRegime regime) {
    return Stock.of("테스트종목", "005930", regime, StockTrend.UPTREND);
  }
}
