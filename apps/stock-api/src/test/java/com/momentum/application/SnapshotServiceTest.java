package com.momentum.application;

import static com.momentum.domain.SnapshotJudgment.BUY;
import static com.momentum.domain.SnapshotJudgment.SELL;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.support.error.CoreException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class SnapshotServiceTest {

  private static final LocalDateTime RECORDED_AT = LocalDateTime.of(2026, 5, 10, 9, 30);

  @Autowired
  private SnapshotService snapshotService;
  @Autowired
  private SnapshotRepository snapshotRepository;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;

  @Test
  @DisplayName("생성 시 종목의 현재 레짐과 최신 종가를 박제해 저장한다")
  void createCapturesRegimeAndPrice() {
    Stock stock = saveStock("000040", BREAKOUT_READY);
    saveCandle(stock, 10_000L);
    SnapshotCreateRequest request = new SnapshotCreateRequest(stock.getCode(), BUY, List.of(), "회고");

    SnapshotCreateResponse response = snapshotService.create(request);

    StockSnapShot saved = snapshotRepository.findById(response.snapshotId()).orElseThrow();
    assertThat(saved.getCapturedRegime()).isEqualTo(BREAKOUT_READY);
    assertThat(saved.getCapturedPrice()).isEqualTo(10_000L);
    assertThat(saved.getJudgment()).isEqualTo(BUY);
  }

  @Test
  @DisplayName("존재하지 않는 종목으로 생성하면 예외")
  void createThrowsWhenStockNotFound() {
    SnapshotCreateRequest request = new SnapshotCreateRequest("000270", BUY, List.of(), "회고");

    assertThatThrownBy(() -> snapshotService.create(request))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("상세 조회는 참고 스냅샷 id·회고를 반환한다")
  void getDetailReturnsReferencesAndRetrospective() {
    Stock stock = saveStock("000050", BREAKOUT_READY);
    StockSnapShot ref = saveSnapshot(stock, 9_000L, BUY);
    StockSnapShot snapshot = snapshotRepository.save(
        StockSnapShot.create(stock, 10_000L, BUY, List.of(ref), RECORDED_AT, "회고내용"));

    SnapshotDetailResponse detail = snapshotService.getDetail(snapshot.getId());

    assertThat(detail.referenceSnapshotIds()).containsExactly(ref.getId());
    assertThat(detail.retrospective()).isEqualTo("회고내용");
    assertThat(detail.judgment()).isEqualTo(BUY);
    assertThat(detail.stockName()).isEqualTo(stock.getName());
    assertThat(detail.stockCode()).isEqualTo(stock.getCode());
  }

  @Test
  @DisplayName("존재하지 않는 스냅샷 상세 조회는 예외")
  void getDetailThrowsWhenNotFound() {
    assertThatThrownBy(() -> snapshotService.getDetail(999_999L))
        .isInstanceOf(CoreException.class);
  }

  @Test
  @DisplayName("목록은 레짐 필터로 추리고 판단별 카운트를 집계한다")
  void getSnapShotsFiltersByRegimeAndCounts() {
    Stock ready = saveStock("000070", BREAKOUT_READY);
    Stock success = saveStock("000227", BREAKOUT_SUCCESS);
    saveSnapshot(ready, 100L, BUY);
    saveSnapshot(ready, 200L, SELL);
    saveSnapshot(success, 300L, BUY);

    SnapshotListResponse response =
        snapshotService.getSnapShots(null, null, null, List.of(BREAKOUT_READY), null);

    assertThat(response.snapshots()).hasSize(2);
    assertThat(response.buyCount()).isEqualTo(1);
    assertThat(response.sellCount()).isEqualTo(1);
    assertThat(response.watchCount()).isZero();
  }

  @Test
  @DisplayName("수정은 판단·회고만 바꾸고 박제값은 유지한다")
  void updateKeepsCapturedValues() {
    Stock stock = saveStock("000540", BREAKOUT_READY);
    StockSnapShot snapshot = saveSnapshot(stock, 10_000L, BUY);
    SnapshotUpdateRequest request =
        new SnapshotUpdateRequest(snapshot.getId(), SELL, List.of(), "수정된 회고");

    snapshotService.update(snapshot.getId(), request);

    StockSnapShot updated = snapshotRepository.findById(snapshot.getId()).orElseThrow();
    assertThat(updated.getJudgment()).isEqualTo(SELL);
    assertThat(updated.getRetrospective()).isEqualTo("수정된 회고");
    assertThat(updated.getCapturedPrice()).isEqualTo(10_000L);
    assertThat(updated.getCapturedRegime()).isEqualTo(BREAKOUT_READY);
  }

  private Stock saveStock(String code, StockRegime regime) {
    return stockRepository.save(Stock.of("종목" + code, code, regime, StockTrend.UPTREND));
  }

  private void saveCandle(Stock stock, long closePrice) {
    stockCandleRepository.save(
        StockDailyCandle.create(stock, "20260510", closePrice, closePrice, closePrice, closePrice, 1_000L, "2"));
  }

  private StockSnapShot saveSnapshot(Stock stock, long price, SnapshotJudgment judgment) {
    return snapshotRepository.save(
        StockSnapShot.create(stock, price, judgment, List.of(), RECORDED_AT, "회고"));
  }
}
