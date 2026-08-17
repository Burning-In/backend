package com.momentum.infrastructure.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockSubscriptionQueryDao {

  private static final String SUBSCRIPTION_STOCK_SQL = """
      select s.code, s.name
        from stock s
       where s.deleted_at is null
       order by s.code asc
      """;

  private final JdbcTemplate jdbcTemplate;

  public List<SubscriptionStockRow> findAll() {
    return jdbcTemplate.query(SUBSCRIPTION_STOCK_SQL,
        (rs, rowNum) -> new SubscriptionStockRow(rs.getString("code"), rs.getString("name")));
  }
}
