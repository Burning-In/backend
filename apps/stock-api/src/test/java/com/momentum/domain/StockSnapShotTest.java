package com.momentum.domain;

import static com.momentum.domain.SnapshotJudgment.BUY;
import static com.momentum.domain.SnapshotJudgment.SELL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.sharedkernel.StockRegime;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockSnapShotTest {

  private static final LocalDateTime RECORDED_AT = LocalDateTime.of(2026, 5, 10, 9, 30);
  private static final long STOCK_ID = 1L;
  private static final StockRegime CAPTURED_REGIME = StockRegime.BREAKOUT_READY;

  @Test
  @DisplayName("create는 생성 시점의 종목 레짐·가격을 박제한다")
  void captureFreezesRegimeAndPrice() {
    StockSnapShot snapshot = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

    assertThat(snapshot.getCapturedRegime()).isEqualTo(StockRegime.BREAKOUT_READY);
    assertThat(snapshot.getCapturedPrice()).isEqualTo(10_000L);
    assertThat(snapshot.getJudgment()).isEqualTo(BUY);
    assertThat(snapshot.getRecordedAt()).isEqualTo(RECORDED_AT);
    assertThat(snapshot.getRetrospective()).isEqualTo("회고");
  }

  @Test
  @DisplayName("create는 참고 스냅샷을 SnapshotReference 링크로 연결한다")
  void createLinksReferences() {
    StockSnapShot ref1 = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 9_000L, BUY, List.of(), RECORDED_AT, "ref1");
    StockSnapShot ref2 = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 9_500L, BUY, List.of(), RECORDED_AT, "ref2");

    StockSnapShot snapshot = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 10_000L, BUY, List.of(ref1, ref2), RECORDED_AT, "회고");

    assertThat(snapshot.getReferences())
        .extracting(SnapshotReference::getReferenced)
        .containsExactly(ref1, ref2);
  }

  @Test
  @DisplayName("update는 판단·참고·회고만 바꾸고 박제값(레짐·가격·기록시점)은 유지한다")
  void updateKeepsCapturedValues() {
    StockSnapShot ref = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 9_000L, BUY, List.of(), RECORDED_AT, "ref");
    StockSnapShot snapshot = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

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
    StockSnapShot snapshot = StockSnapShot.create(STOCK_ID, CAPTURED_REGIME, 10_000L, BUY, List.of(), RECORDED_AT, "회고");

    assertThatThrownBy(() -> snapshot.update(BUY, List.of(snapshot), "회고"))
        .isInstanceOf(IllegalArgumentException.class);
  }

}
