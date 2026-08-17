package com.momentum.infrastructure.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MovingAverageQueryDao {

  private static final String SQL = """
      select c.close_price as current_price,
             max(case when m.stock_moving_average_period = 'MA_50'  then m.ma end) as ma50,
             max(case when m.stock_moving_average_period = 'MA_150' then m.ma end) as ma150,
             max(case when m.stock_moving_average_period = 'MA_200' then m.ma end) as ma200
        from stock s
        join stock_daily_candle c
          on c.stock_id = s.id
         and c.trade_date = (select max(c2.trade_date)
                               from stock_daily_candle c2
                              where c2.stock_id = s.id
                                and c2.trade_date <= ?)
        left join stock_moving_average m
          on m.stock_id = s.id
         and m.deleted_at is null
         and m.base_date = (select max(m2.base_date)
                              from stock_moving_average m2
                             where m2.stock_id = s.id
                               and m2.deleted_at is null
                               and m2.base_date <= ?)
       where s.code = ?
       group by c.close_price
      """;

  private final JdbcTemplate jdbcTemplate;

  public MovingAverageRow findByStockCode(String stockCode, LocalDate at) {
    return Optional.ofNullable(
            jdbcTemplate.query(SQL, MovingAverageQueryDao::toRow, at, at, stockCode))
        .orElseThrow(() -> new NoSuchElementException("이동평균 조회 대상이 없습니다: " + stockCode));
  }

  private static MovingAverageRow toRow(ResultSet rs) throws SQLException {
    if (!rs.next()) {
      return null;
    }
    return new MovingAverageRow(
        rs.getLong("current_price"),
        nullableLong(rs, "ma50"),
        nullableLong(rs, "ma150"),
        nullableLong(rs, "ma200"));
  }

  private static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    if (rs.wasNull()) {
      return null;
    }
    return value;
  }
}
