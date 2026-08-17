package com.momentum.support;

import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * stock-batch 가 소유한 분석 도메인 테이블에 테스트 데이터를 넣는다. stock-api 는 이 테이블들을 읽기만 하므로 엔티티가 없다.
 */
@Component
public class AnalysisTestData {

  private final JdbcTemplate jdbcTemplate;

  public AnalysisTestData(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public long saveStock(String code, String name, StockRegime regime, StockTrend trend) {
    jdbcTemplate.update("""
        insert into stock (code, name, stock_regime, stock_trend, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?)
        """, code, name, regime.name(), trend.name(), now(), now());
    return lastId("stock");
  }

  public long saveStock(String code, StockRegime regime) {
    return saveStock(code, "종목" + code, regime, StockTrend.UPTREND);
  }

  public void saveCandle(long stockId, LocalDate tradeDate, long closePrice, long volume) {
    saveCandle(stockId, tradeDate, closePrice, closePrice, closePrice, closePrice, volume);
  }

  public void saveCandle(long stockId, LocalDate tradeDate, long open, long high, long low, long close, long volume) {
    jdbcTemplate.update("""
        insert into stock_daily_candle (trade_date, open_price, high_price, low_price, close_price, volume,
                                        stock_id, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, tradeDate, open, high, low, close, volume, stockId, now(), now());
  }

  public void saveMovingAverage(long stockId, StockMovingAveragePeriod period, long ma, LocalDate baseDate) {
    jdbcTemplate.update("""
        insert into stock_moving_average (ma, base_date, stock_moving_average_period, stock_id, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?)
        """, ma, baseDate, period.name(), stockId, now(), now());
  }

  public long saveBase(long stockId, long supportPrice, long resistancePrice, LocalDate startedAt) {
    long supportLineId = saveBaseLine(supportPrice, "SUPPORT");
    long resistanceLineId = saveBaseLine(resistancePrice, "RESISTANCE");
    jdbcTemplate.update("""
        insert into stock_base (is_vcp, started_at, stage_level, stock_base_kind, stock_id,
                                highest_resistance_line_id, lowest_support_line_id, created_at, updated_at)
        values (false, ?, 1, 'BASE', ?, ?, ?, ?, ?)
        """, startedAt, stockId, resistanceLineId, supportLineId, now(), now());
    return lastId("stock_base");
  }

  public void saveRankScore(long stockId, LocalDate baseDate, double momentum, double fip, int upDays, int downDays) {
    jdbcTemplate.update("""
        insert into stock_rank_score (base_date, momentum, fip, up_days, down_days, stock_id, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?, ?, ?)
        """, baseDate, momentum, fip, upDays, downDays, stockId, now(), now());
  }

  public void saveEps(long stockId, YearMonth quarter, double eps, Double yearOverYearChangeRate) {
    jdbcTemplate.update("""
        insert into stock_eps (eps, quarter, year_over_year_change_rate, stock_id, created_at, updated_at)
        values (?, ?, ?, ?, ?, ?)
        """, eps, quarter.atDay(1), yearOverYearChangeRate, stockId, now(), now());
  }

  public void saveRelativeStrength(long stockId, int rsScore) {
    jdbcTemplate.update("""
        insert into kospi_relative_strength (rs_score, stock_id, created_at, updated_at)
        values (?, ?, ?, ?)
        """, rsScore, stockId, now(), now());
  }

  public void saveTick(String stockCode, long price) {
    jdbcTemplate.update("""
        insert into stock_tick (stock_code, trade_time, price, volume, acc_volume, created_at, updated_at)
        values (?, ?, ?, 1, 1, ?, ?)
        """, stockCode, LocalDate.now().atStartOfDay(), price, now(), now());
  }

  private long saveBaseLine(long price, String type) {
    jdbcTemplate.update("""
        insert into stock_base_line (price, strength, accumulated_volume, touch_count, type, created_at, updated_at)
        values (?, 1, 100000, 1, ?, ?, ?)
        """, price, type, now(), now());
    return lastId("stock_base_line");
  }

  private long lastId(String table) {
    return jdbcTemplate.queryForObject("select max(id) from " + table, Long.class);
  }

  private OffsetDateTime now() {
    return ZonedDateTime.now().toOffsetDateTime();
  }
}
