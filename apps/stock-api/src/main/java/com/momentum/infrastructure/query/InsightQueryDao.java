package com.momentum.infrastructure.query;

import com.momentum.infrastructure.query.InsightRows.CandlePriceRow;
import com.momentum.infrastructure.query.InsightRows.EpsRow;
import com.momentum.infrastructure.query.InsightRows.RankScoreRow;
import com.momentum.infrastructure.query.InsightRows.RegimeRow;
import com.momentum.infrastructure.query.InsightRows.VolumeRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InsightQueryDao {

  private static final String CURRENT_BASE = """
        left join stock_base b
          on b.id = (select b2.id
                       from stock_base b2
                      where b2.stock_id = s.id
                        and b2.deleted_at is null
                        and b2.stock_base_kind = 'BASE'
                      order by b2.started_at desc, b2.id desc
                      limit 1)
      """;

  private static final String RECENT_CANDLE = """
        join stock_daily_candle c
          on c.stock_id = s.id
         and c.trade_date = (select max(c2.trade_date)
                               from stock_daily_candle c2
                              where c2.stock_id = s.id
                                and c2.trade_date <= ?)
      """;

  private static final String REGIME_SQL = "select s.stock_regime, c.close_price, support.price as support_price, "
      + "resistance.price as resistance_price from stock s "
      + RECENT_CANDLE + CURRENT_BASE
      + " left join stock_base_line support on support.id = b.lowest_support_line_id"
      + " left join stock_base_line resistance on resistance.id = b.highest_resistance_line_id"
      + " where s.code = ?";

  private static final String VOLUME_SQL = "select c.volume, b.started_at from stock s "
      + RECENT_CANDLE + CURRENT_BASE + " where s.code = ?";

  private static final String RANK_SCORE_SQL = """
      select r.base_date, r.momentum, r.fip, r.up_days, r.down_days
        from stock_rank_score r
        join stock s on s.id = r.stock_id
       where s.code = ?
         and r.deleted_at is null
       order by r.base_date desc, r.id desc
       limit 1
      """;

  private static final String MOMENTUM_PERCENTILE_SQL = """
      select r.momentum
        from stock_rank_score r
       where r.base_date = ?
         and r.deleted_at is null
         and r.momentum is not null
      """;

  private static final String FIP_PERCENTILE_SQL = """
      select r.fip
        from stock_rank_score r
       where r.base_date = ?
         and r.deleted_at is null
         and r.fip is not null
      """;

  private static final String RECENT_CLOSES_SQL = """
      select c.trade_date, c.close_price
        from stock_daily_candle c
        join stock s on s.id = c.stock_id
       where s.code = ?
         and c.trade_date <= ?
       order by c.trade_date desc
       limit ?
      """;

  private static final String EPS_SQL = """
      select e.quarter, e.eps, e.year_over_year_change_rate
        from stock_eps e
        join stock s on s.id = e.stock_id
       where s.code = ?
         and e.deleted_at is null
       order by e.quarter desc
       limit ?
      """;

  private static final String RS_SQL = """
      select rs.rs_score
        from kospi_relative_strength rs
        join stock s on s.id = rs.stock_id
       where s.code = ?
         and rs.deleted_at is null
       order by rs.created_at desc, rs.id desc
       limit 1
      """;

  private static final String AVERAGE_VOLUME_SQL = """
      select avg(c.volume)
        from stock_daily_candle c
        join stock s on s.id = c.stock_id
       where s.code = ?
         and c.deleted_at is null
         and c.trade_date >= ?
         and c.trade_date <= ?
      """;

  private static final RowMapper<CandlePriceRow> CLOSE_MAPPER = (rs, rowNum) -> new CandlePriceRow(
      rs.getObject("trade_date", LocalDate.class), rs.getLong("close_price"));

  private final JdbcTemplate jdbcTemplate;

  public Optional<RegimeRow> findRegime(String stockCode, LocalDate at) {
    return jdbcTemplate.query(REGIME_SQL, (rs, rowNum) -> new RegimeRow(
        rs.getString("stock_regime"),
        rs.getLong("close_price"),
        nullableLong(rs.getObject("support_price")),
        nullableLong(rs.getObject("resistance_price"))), at, stockCode).stream().findFirst();
  }

  public Optional<VolumeRow> findVolume(String stockCode, LocalDate at) {
    return jdbcTemplate.query(VOLUME_SQL, (rs, rowNum) -> new VolumeRow(
        rs.getLong("volume"),
        rs.getObject("started_at", LocalDate.class)), at, stockCode).stream().findFirst();
  }

  public Optional<RankScoreRow> findLatestRankScore(String stockCode) {
    return jdbcTemplate.query(RANK_SCORE_SQL, (rs, rowNum) -> new RankScoreRow(
        rs.getObject("base_date", LocalDate.class),
        rs.getBigDecimal("momentum"),
        rs.getBigDecimal("fip"),
        rs.getInt("up_days"),
        rs.getInt("down_days")), stockCode).stream().findFirst();
  }

  public List<BigDecimal> findMomentumScores(LocalDate baseDate) {
    return jdbcTemplate.queryForList(MOMENTUM_PERCENTILE_SQL, BigDecimal.class, baseDate);
  }

  public List<BigDecimal> findFipScores(LocalDate baseDate) {
    return jdbcTemplate.queryForList(FIP_PERCENTILE_SQL, BigDecimal.class, baseDate);
  }

  public List<CandlePriceRow> findRecentCloses(String stockCode, LocalDate at, int limit) {
    return jdbcTemplate.query(RECENT_CLOSES_SQL, CLOSE_MAPPER, stockCode, at, limit);
  }

  public List<EpsRow> findRecentEps(String stockCode, int limit) {
    return jdbcTemplate.query(EPS_SQL, (rs, rowNum) -> new EpsRow(
        rs.getObject("quarter", LocalDate.class),
        rs.getDouble("eps"),
        (Double) rs.getObject("year_over_year_change_rate")), stockCode, limit);
  }

  public Optional<Integer> findLatestRsScore(String stockCode) {
    return jdbcTemplate.queryForList(RS_SQL, Integer.class, stockCode).stream().findFirst();
  }

  public Long averageVolume(String stockCode, LocalDate from, LocalDate to) {
    Double average = jdbcTemplate.queryForObject(AVERAGE_VOLUME_SQL, Double.class, stockCode, from, to);
    if (average == null) {
      return null;
    }
    return average.longValue();
  }

  private static Long nullableLong(Object value) {
    if (value == null) {
      return null;
    }
    return ((Number) value).longValue();
  }
}
