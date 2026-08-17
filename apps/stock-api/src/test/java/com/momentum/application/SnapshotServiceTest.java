package com.momentum.application;

import static com.momentum.domain.SnapshotJudgment.BUY;
import static com.momentum.domain.SnapshotJudgment.SELL;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse.SnapshotListItem;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import com.momentum.support.AnalysisTestData;
import com.momentum.support.error.CoreException;
import java.time.LocalDate;
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
  private AnalysisTestData analysisTestData;
  @Autowired
  private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("생성 시 종목의 현재 레짐과 최신 종가를 박제해 저장한다")
  void createCapturesRegimeAndPrice() {
    long stockId = saveStock("000040", BREAKOUT_READY);
    saveCandle(stockId, 10_000L);
    SnapshotCreateRequest request = new SnapshotCreateRequest("000040", BUY, List.of(), "회고");

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
    long stockId = saveStock("000050", BREAKOUT_READY);
    StockSnapShot ref = saveSnapshot(stockId, 9_000L, BUY);
    StockSnapShot snapshot = snapshotRepository.save(
        StockSnapShot.create(stockId, BREAKOUT_READY, 10_000L, BUY, List.of(ref), RECORDED_AT, "회고내용"));

    SnapshotDetailResponse detail = snapshotService.getDetail(snapshot.getId());

    assertThat(detail.referenceSnapshotIds()).containsExactly(ref.getId());
    assertThat(detail.retrospective()).isEqualTo("회고내용");
    assertThat(detail.judgment()).isEqualTo(BUY);
    assertThat(detail.stockName()).isEqualTo("종목000050");
    assertThat(detail.stockCode()).isEqualTo("000050");
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
    long readyStockId = saveStock("000070", BREAKOUT_READY);
    long successStockId = saveStock("000227", BREAKOUT_SUCCESS);
    saveSnapshot(readyStockId, 100L, BUY);
    saveSnapshot(readyStockId, 200L, SELL);
    saveSnapshot(successStockId, 300L, BUY);

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
    long stockId = saveStock("000540", BREAKOUT_READY);
    StockSnapShot snapshot = saveSnapshot(stockId, 10_000L, BUY);
    SnapshotUpdateRequest request =
        new SnapshotUpdateRequest(snapshot.getId(), SELL, List.of(), "수정된 회고");

    snapshotService.update(snapshot.getId(), request);

    StockSnapShot updated = snapshotRepository.findById(snapshot.getId()).orElseThrow();
    assertThat(updated.getJudgment()).isEqualTo(SELL);
    assertThat(updated.getRetrospective()).isEqualTo("수정된 회고");
    assertThat(updated.getCapturedPrice()).isEqualTo(10_000L);
    assertThat(updated.getCapturedRegime()).isEqualTo(BREAKOUT_READY);
  }

  @Test
  @DisplayName("목록은 종목명 부분 일치로 추리고, 접두 일치를 앞에 둔다")
  void getSnapShotsFiltersByStockNameAndPutsPrefixMatchFirst() {
    long partialMatchStockId = saveStock("000660", "미래삼성전자", BREAKOUT_READY);
    long prefixMatchStockId = saveStock("005930", "삼성전자", BREAKOUT_READY);
    long noMatchStockId = saveStock("000810", "현대차", BREAKOUT_READY);
    saveSnapshot(partialMatchStockId, 100L, BUY);
    saveSnapshot(prefixMatchStockId, 200L, BUY);
    saveSnapshot(noMatchStockId, 300L, BUY);

    SnapshotListResponse response =
        snapshotService.getSnapShots(null, null, null, null, "삼성");

    assertThat(response.snapshots())
        .extracting(SnapshotListItem::stockName)
        .containsExactly("삼성전자", "미래삼성전자");
  }

  @Test
  @DisplayName("목록은 판단 필터로 추린다")
  void getSnapShotsFiltersByJudgment() {
    long stockId = saveStock("001450", BREAKOUT_READY);
    saveSnapshot(stockId, 100L, BUY);
    saveSnapshot(stockId, 200L, SELL);

    SnapshotListResponse response =
        snapshotService.getSnapShots(null, null, List.of(SELL), null, null);

    assertThat(response.snapshots())
        .extracting(SnapshotListItem::judgment)
        .containsExactly(SELL);
  }

  @Test
  @DisplayName("목록은 기록 시각 구간 밖의 스냅샷을 제외하고 최신순으로 정렬한다")
  void getSnapShotsFiltersByRecordedRangeAndSortsByLatest() {
    long stockId = saveStock("002710", BREAKOUT_READY);
    saveSnapshotAt(stockId, 100L, RECORDED_AT.minusDays(2));
    saveSnapshotAt(stockId, 200L, RECORDED_AT);
    saveSnapshotAt(stockId, 300L, RECORDED_AT.plusDays(2));

    SnapshotListResponse response = snapshotService.getSnapShots(
        RECORDED_AT.minusDays(1), RECORDED_AT.plusDays(1), null, null, null);

    assertThat(response.snapshots())
        .extracting(SnapshotListItem::price)
        .containsExactly(200L);
  }

  private long saveStock(String code, StockRegime regime) {
    return analysisTestData.saveStock(code, regime);
  }

  private long saveStock(String code, String name, StockRegime regime) {
    return analysisTestData.saveStock(code, name, regime, com.momentum.sharedkernel.StockTrend.UPTREND);
  }

  private StockSnapShot saveSnapshotAt(long stockId, long price, LocalDateTime recordedAt) {
    return snapshotRepository.save(
        StockSnapShot.create(stockId, regimeOf(stockId), price, BUY, List.of(), recordedAt, "회고"));
  }

  private void saveCandle(long stockId, long closePrice) {
    analysisTestData.saveCandle(stockId, LocalDate.now(), closePrice, 1_000L);
  }

  private StockSnapShot saveSnapshot(long stockId, long price, SnapshotJudgment judgment) {
    return snapshotRepository.save(
        StockSnapShot.create(stockId, regimeOf(stockId), price, judgment, List.of(), RECORDED_AT, "회고"));
  }

  private StockRegime regimeOf(long stockId) {
    return StockRegime.valueOf(
        jdbcTemplate.queryForObject("select stock_regime from stock where id = ?", String.class, stockId));
  }
}
