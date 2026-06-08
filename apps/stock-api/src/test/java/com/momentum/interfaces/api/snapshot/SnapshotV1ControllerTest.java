package com.momentum.interfaces.api.snapshot;

import static com.momentum.domain.SnapshotJudgment.BUY;
import static com.momentum.domain.SnapshotJudgment.SELL;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SnapshotV1ControllerTest {

  private static final LocalDateTime RECORDED_AT = LocalDateTime.of(2026, 5, 10, 9, 30);

  @Autowired
  private MockMvcTester mockMvcTester;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private SnapshotRepository snapshotRepository;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockCandleRepository stockCandleRepository;

  @Test
  @DisplayName("스냅샷 생성 API 해피케이스")
  void createSnapshot() throws Exception {
    Stock stock = saveStock("000001", BREAKOUT_READY);
    saveCandle(stock, 10_000L);
    String body = objectMapper.writeValueAsString(
        new SnapshotCreateRequest(stock.getCode(), BUY, List.of(), "회고"));

    assertThat(mockMvcTester.post()
        .uri("/api/v1/snapshots")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.snapshotId").isNotNull();
  }

  @Test
  @DisplayName("스냅샷 상세 조회 API 해피케이스")
  void getSnapshotDetail() {
    Stock stock = saveStock("000002", BREAKOUT_READY);
    StockSnapShot snapshot = snapshotRepository.save(
        StockSnapShot.create(stock, 10_000L, BUY, List.of(), RECORDED_AT, "회고내용"));

    assertThat(mockMvcTester.get().uri("/api/v1/snapshots/{id}", snapshot.getId()))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.retrospective").isEqualTo("회고내용");
  }

  @Test
  @DisplayName("스냅샷 목록 조회 API 해피케이스 (레짐 필터 + 카운트)")
  void getSnapshotList() {
    Stock stock = saveStock("000003", BREAKOUT_READY);
    saveSnapshot(stock, 100L, BUY);
    saveSnapshot(stock, 200L, SELL);

    assertThat(mockMvcTester.get().uri("/api/v1/snapshots").param("regimes", "BREAKOUT_READY"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.buyCount").isEqualTo(1);
  }

  @Test
  @DisplayName("스냅샷 수정 API 해피케이스")
  void updateSnapshot() throws Exception {
    Stock stock = saveStock("000004", BREAKOUT_READY);
    StockSnapShot snapshot = saveSnapshot(stock, 10_000L, BUY);
    String body = objectMapper.writeValueAsString(
        new SnapshotUpdateRequest(snapshot.getId(), SELL, List.of(), "수정된 회고"));

    assertThat(mockMvcTester.patch()
        .uri("/api/v1/snapshots/{id}", snapshot.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.meta.result").isEqualTo("SUCCESS");
  }

  private Stock saveStock(String code, StockRegime regime) {
    return stockRepository.save(new Stock("종목" + code, code, regime, StockTrend.UPTREND));
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
