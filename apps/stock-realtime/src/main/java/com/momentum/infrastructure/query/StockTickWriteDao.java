package com.momentum.infrastructure.query;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockTickWriteDao {

  private static final String INSERT_SQL = """
      insert into stock_tick (stock_code, trade_time, price, volume, acc_volume, created_at, updated_at)
      values (?, ?, ?, ?, ?, ?, ?)
      """;

  private final JdbcTemplate jdbcTemplate;

  public void insert(String stockCode, LocalDateTime tradeTime, long price, long volume, long accVolume) {
    ZonedDateTime now = ZonedDateTime.now();
    jdbcTemplate.update(INSERT_SQL, stockCode, tradeTime, price, volume, accVolume,
        now.toOffsetDateTime(), now.toOffsetDateTime());
  }
}
