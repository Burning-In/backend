package com.momentum.infrastructure.query;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockChartQueryDao {

  private static final String CANDLE_SQL = """
      select c.trade_date, c.open_price, c.high_price, c.low_price, c.close_price, c.volume
        from stock_daily_candle c
        join stock s on s.id = c.stock_id
       where s.code = ?
         and c.deleted_at is null
         and (cast(? as date) is null or c.trade_date >= ?)
         and c.trade_date <= ?
       order by c.trade_date asc
      """;

  private static final String BASE_SQL = """
      select b.started_at, support.price as support_price, resistance.price as resistance_price
        from stock_base b
        join stock s on s.id = b.stock_id
        join stock_base_line support on support.id = b.lowest_support_line_id
        join stock_base_line resistance on resistance.id = b.highest_resistance_line_id
       where s.code = ?
         and b.deleted_at is null
       order by b.started_at asc, b.id asc
      """;

  private static final RowMapper<DailyCandleRow> CANDLE_MAPPER = (rs, rowNum) -> new DailyCandleRow(
      rs.getObject("trade_date", LocalDate.class),
      rs.getLong("open_price"),
      rs.getLong("high_price"),
      rs.getLong("low_price"),
      rs.getLong("close_price"),
      rs.getLong("volume"));

  private static final RowMapper<BaseRow> BASE_MAPPER = (rs, rowNum) -> new BaseRow(
      rs.getObject("started_at", LocalDate.class),
      rs.getLong("support_price"),
      rs.getLong("resistance_price"));

  private final JdbcTemplate jdbcTemplate;

  public List<DailyCandleRow> findCandles(String stockCode, LocalDate from, LocalDate to) {
    return jdbcTemplate.query(CANDLE_SQL, CANDLE_MAPPER, stockCode, from, from, to);
  }

  public List<BaseRow> findBases(String stockCode) {
    return jdbcTemplate.query(BASE_SQL, BASE_MAPPER, stockCode);
  }

  public boolean existsStock(String stockCode) {
    Integer count = jdbcTemplate.queryForObject(
        "select count(1) from stock where code = ? and deleted_at is null", Integer.class, stockCode);
    return count != null && count > 0;
  }
}
