package com.momentum.infrastructure.query;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RealtimeRankingQueryDao {

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

  private final JdbcTemplate jdbcTemplate;

  public List<RankedStockRow> findRanked(String stockRegime, LocalDate tradeDate, int limit) {
    return jdbcTemplate.query(RANKED_STOCK_SQL, (rs, rowNum) -> new RankedStockRow(
        rs.getString("name"),
        rs.getString("code"),
        rs.getBigDecimal("momentum"),
        rs.getBigDecimal("fip")), stockRegime, tradeDate, limit);
  }
}
