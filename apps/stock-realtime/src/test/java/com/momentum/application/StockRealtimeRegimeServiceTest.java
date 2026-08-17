package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static com.momentum.sharedkernel.StockTrend.UPTREND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.infrastructure.query.RealtimeRegimeQueryDao;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockRealtimeRegimeServiceTest {

  private static final long RESISTANCE_PRICE = 10_000L;
  private static final long SUPPORT_PRICE = 8_000L;
  // 저항선 상단 = 10_000 * 1.05 = 10_500, 지지선 하단 = 8_000 * 0.95 = 7_600
  private static final long ABOVE_RESISTANCE_UPPER_BOUND = 11_000L;
  private static final long INSIDE_BASE_PRICE = 10_000L;

  private static final String TRACKED_CODE = "000040";
  private static final String UNKNOWN_CODE = "000270";

  @Autowired
  private StockRealtimeRegimeService stockRealtimeRegimeService;
  @Autowired
  private RealtimeRegimeQueryDao realtimeRegimeQueryDao;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("delete from stock_anchor_point");
    jdbcTemplate.update("delete from stock_base");
    jdbcTemplate.update("delete from stock_base_line");
    jdbcTemplate.update("delete from stock");
  }

  @Test
  @DisplayName("종목을 찾지 못하면 IllegalArgumentException")
  void throwsWhenStockNotFound() {
    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime(UNKNOWN_CODE, 10_000L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재 베이스가 없으면 IllegalStateException")
  void throwsWhenBaseNotFound() {
    long stockId = saveStock(TRACKED_CODE, BREAKOUT_READY);
    saveAnchorPoint(stockId, 9_300L, 1);

    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime(TRACKED_CODE, 10_000L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("베이스는 있지만 유효한 가격 특이점이 없으면 IllegalStateException")
  void throwsWhenLastAnchorPointNotFound() {
    long stockId = saveStock(TRACKED_CODE, BREAKOUT_READY);
    saveVcpBase(stockId);

    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime(TRACKED_CODE, 10_000L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("소프트 삭제된 특이점은 판정 재료로 쓰이지 않는다")
  void ignoresDeletedAnchorPoint() {
    long stockId = saveStock(TRACKED_CODE, BREAKOUT_READY);
    saveVcpBase(stockId);
    deleteAnchorPoint(saveAnchorPoint(stockId, 9_300L, 1));

    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime(TRACKED_CODE, 10_000L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("VCP이고 현재가가 저항선 상단을 돌파하면 BREAKOUT_SUCCESS로 갱신된다")
  void updatesToBreakoutSuccess() {
    long stockId = saveStock(TRACKED_CODE, BREAKOUT_READY);
    saveVcpBase(stockId);
    saveAnchorPoint(stockId, 9_300L, 1);

    stockRealtimeRegimeService.resolveRealtimeRegime(TRACKED_CODE, ABOVE_RESISTANCE_UPPER_BOUND);

    assertThat(savedRegimeOf(TRACKED_CODE)).isEqualTo(BREAKOUT_SUCCESS.name());
  }

  @Test
  @DisplayName("판정이 UNKNOWN이면 기존 레짐을 그대로 둔다")
  void keepsRegimeWhenUnknown() {
    long stockId = saveStock(TRACKED_CODE, BREAKOUT_READY);
    saveVcpBase(stockId);
    saveAnchorPoint(stockId, 9_300L, 1);

    stockRealtimeRegimeService.resolveRealtimeRegime(TRACKED_CODE, INSIDE_BASE_PRICE);

    assertThat(savedRegimeOf(TRACKED_CODE)).isEqualTo(BREAKOUT_READY.name());
  }

  private String savedRegimeOf(String stockCode) {
    return realtimeRegimeQueryDao.findRegimeSource(stockCode).orElseThrow().stockRegime();
  }

  private long saveStock(String code, StockRegime regime) {
    jdbcTemplate.update("""
        insert into stock (code, name, stock_regime, stock_trend, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?)
        """, code, "테스트종목", regime.name(), UPTREND.name(), now(), now());
    return jdbcTemplate.queryForObject("select id from stock where code = ?", Long.class, code);
  }

  private void saveVcpBase(long stockId) {
    long resistanceLineId = saveBaseLine(RESISTANCE_PRICE, "RESISTANCE");
    long supportLineId = saveBaseLine(SUPPORT_PRICE, "SUPPORT");
    jdbcTemplate.update("""
        insert into stock_base (is_vcp, started_at, stage_level, stock_base_kind, stock_id,
                                highest_resistance_line_id, lowest_support_line_id, created_at, updated_at)
        values (true, ?, 1, 'BASE', ?, ?, ?, ?, ?)
        """, LocalDate.now().minusDays(20), stockId, resistanceLineId, supportLineId, now(), now());
  }

  private long saveBaseLine(long price, String type) {
    jdbcTemplate.update("""
        insert into stock_base_line (price, strength, accumulated_volume, touch_count, type, created_at, updated_at)
        values (?, 1, 100000, 1, ?, ?, ?)
        """, price, type, now(), now());
    return jdbcTemplate.queryForObject("select max(id) from stock_base_line", Long.class);
  }

  private long saveAnchorPoint(long stockId, long price, int daysAgo) {
    jdbcTemplate.update("""
        insert into stock_anchor_point (price, volume, trade_date, type, stock_id, created_at, updated_at)
        values (?, 100000, ?, 'HIGH', ?, ?, ?)
        """, price, LocalDate.now().minusDays(daysAgo), stockId, now(), now());
    return jdbcTemplate.queryForObject("select max(id) from stock_anchor_point", Long.class);
  }

  private void deleteAnchorPoint(long anchorPointId) {
    jdbcTemplate.update("update stock_anchor_point set deleted_at = ? where id = ?", now(), anchorPointId);
  }

  private OffsetDateTime now() {
    return ZonedDateTime.now().toOffsetDateTime();
  }
}
