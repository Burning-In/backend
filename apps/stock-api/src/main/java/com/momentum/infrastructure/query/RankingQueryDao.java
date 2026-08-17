package com.momentum.infrastructure.query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingQueryDao {

  private static final String RANKED_STOCK_SQL = """
      select s.name, s.code, r.momentum, r.fip
        from stock_rank_score r
        join stock s on s.id = r.stock_id
       where r.deleted_at is null
         and s.stock_regime = ?
         and r.base_date = (select max(r2.base_date)
                              from stock_rank_score r2
                             where r2.stock_id = r.stock_id
                               and r2.deleted_at is null
                               and r2.base_date <= ?)
       order by r.momentum desc, r.fip asc
       limit ?
      """;

  private static final String LATEST_TICK_PRICE_SQL = """
      select t.stock_code, t.price
        from stock_tick t
       where t.stock_code in (%s)
         and t.id = (select t2.id
                       from stock_tick t2
                      where t2.stock_code = t.stock_code
                        and t2.created_at <= ?
                      order by t2.created_at desc, t2.id desc
                      limit 1)
      """;

  private final JdbcTemplate jdbcTemplate;

  public List<RankedStockRow> findRanked(String stockRegime, LocalDate tradeDate, int limit) {
    return jdbcTemplate.query(RANKED_STOCK_SQL, (rs, rowNum) -> new RankedStockRow(
        rs.getString("name"),
        rs.getString("code"),
        rs.getBigDecimal("momentum"),
        rs.getBigDecimal("fip")), stockRegime, tradeDate, limit);
  }

  public Map<String, BigDecimal> findLatestPricesByStockCode(List<String> stockCodes, ZonedDateTime at) {
    if (stockCodes.isEmpty()) {
      return Map.of();
    }
    List<Object> arguments = new ArrayList<>(stockCodes);
    arguments.add(at.toOffsetDateTime());

    return jdbcTemplate.query(
            LATEST_TICK_PRICE_SQL.formatted(placeholders(stockCodes.size())),
            (rs, rowNum) -> Map.entry(rs.getString("stock_code"), BigDecimal.valueOf(rs.getLong("price"))),
            arguments.toArray())
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private String placeholders(int count) {
    return String.join(", ", Collections.nCopies(count, "?"));
  }
}
