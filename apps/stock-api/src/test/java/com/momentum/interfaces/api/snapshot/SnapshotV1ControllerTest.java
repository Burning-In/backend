package com.momentum.interfaces.api.snapshot;

import static com.momentum.domain.SnapshotJudgment.BUY;
import static com.momentum.domain.SnapshotJudgment.SELL;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.infrastructure.auth.JwtAuthenticationFilter;
import com.momentum.infrastructure.auth.JwtProvider;
import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.support.AnalysisTestData;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.time.LocalDate;
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
  private static final long MEMBER_ID = 1L;

  @Autowired
  private MockMvcTester mockMvcTester;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private JwtProvider jwtProvider;
  @Autowired
  private SnapshotRepository snapshotRepository;
  @Autowired
  private AnalysisTestData analysisTestData;

  @Test
  @DisplayName("스냅샷 생성 API 해피케이스")
  void createSnapshot() throws Exception {
    long stockId = saveStock("000040", BREAKOUT_READY);
    saveCandle(stockId, 10_000L);
    String body = objectMapper.writeValueAsString(
        new SnapshotCreateRequest("000040", BUY, List.of(), "회고"));

    assertThat(mockMvcTester.post()
        .uri("/api/v1/snapshots")
        .cookie(accessTokenCookie())
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.snapshotId").isNotNull();
  }

  @Test
  @DisplayName("스냅샷 상세 조회 API 해피케이스")
  void getSnapshotDetail() {
    long stockId = saveStock("000050", BREAKOUT_READY);
    StockSnapShot snapshot = snapshotRepository.save(
        StockSnapShot.create(stockId, BREAKOUT_READY, 10_000L, BUY, List.of(), RECORDED_AT, "회고내용"));

    assertThat(mockMvcTester.get().uri("/api/v1/snapshots/{id}", snapshot.getId())
        .cookie(accessTokenCookie()))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.retrospective").isEqualTo("회고내용");
  }

  @Test
  @DisplayName("스냅샷 목록 조회 API 해피케이스 (레짐 필터 + 카운트)")
  void getSnapshotList() {
    long stockId = saveStock("000070", BREAKOUT_READY);
    saveSnapshot(stockId, 100L, BUY);
    saveSnapshot(stockId, 200L, SELL);

    assertThat(mockMvcTester.get().uri("/api/v1/snapshots")
        .cookie(accessTokenCookie())
        .param("regimes", "BREAKOUT_READY"))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.data.buyCount").isEqualTo(1);
  }

  @Test
  @DisplayName("스냅샷 수정 API 해피케이스")
  void updateSnapshot() throws Exception {
    long stockId = saveStock("000227", BREAKOUT_READY);
    StockSnapShot snapshot = saveSnapshot(stockId, 10_000L, BUY);
    String body = objectMapper.writeValueAsString(
        new SnapshotUpdateRequest(snapshot.getId(), SELL, List.of(), "수정된 회고"));

    assertThat(mockMvcTester.patch()
        .uri("/api/v1/snapshots/{id}", snapshot.getId())
        .cookie(accessTokenCookie())
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .hasStatusOk()
        .bodyJson()
        .extractingPath("$.meta.result").isEqualTo("SUCCESS");
  }

  private Cookie accessTokenCookie() {
    return new Cookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE,
        jwtProvider.createAccessToken(MEMBER_ID, Instant.now()));
  }

  private long saveStock(String code, StockRegime regime) {
    return analysisTestData.saveStock(code, regime);
  }

  private void saveCandle(long stockId, long closePrice) {
    analysisTestData.saveCandle(stockId, LocalDate.now(), closePrice, 1_000L);
  }

  private StockSnapShot saveSnapshot(long stockId, long price, SnapshotJudgment judgment) {
    return snapshotRepository.save(
        StockSnapShot.create(stockId, BREAKOUT_READY, price, judgment, List.of(), RECORDED_AT, "회고"));
  }
}
