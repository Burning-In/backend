package com.momentum.infrastructure.query;

import com.momentum.infrastructure.query.SnapshotRows.SnapshotListRow;
import com.momentum.infrastructure.query.SnapshotRows.SnapshotSourceRow;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SnapshotQueryDao {

  private static final String SNAPSHOT_SOURCE_SQL = """
      select s.id, s.stock_regime, c.close_price
        from stock s
        join stock_daily_candle c
          on c.stock_id = s.id
         and c.trade_date = (select max(c2.trade_date)
                               from stock_daily_candle c2
                              where c2.stock_id = s.id
                                and c2.trade_date <= ?)
       where s.code = ?
         and s.deleted_at is null
      """;

  private static final String SEARCH_SQL = """
      select snap.id, s.name, s.code, snap.captured_regime, snap.judgment,
             snap.recorded_at, snap.captured_price
        from stock_snap_shot snap
        join stock s on s.id = snap.stock_id
       where snap.deleted_at is null
      """;

  private static final RowMapper<SnapshotListRow> LIST_MAPPER = (rs, rowNum) -> new SnapshotListRow(
      rs.getLong("id"),
      rs.getString("name"),
      rs.getString("code"),
      rs.getString("captured_regime"),
      rs.getString("judgment"),
      rs.getObject("recorded_at", LocalDateTime.class),
      rs.getLong("captured_price"));

  private final JdbcTemplate jdbcTemplate;

  public Optional<SnapshotSourceRow> findSnapshotSource(String stockCode, LocalDate at) {
    return jdbcTemplate.query(SNAPSHOT_SOURCE_SQL, (rs, rowNum) -> new SnapshotSourceRow(
        rs.getLong("id"),
        rs.getString("stock_regime"),
        rs.getLong("close_price")), at, stockCode).stream().findFirst();
  }

  public List<SnapshotListRow> search(LocalDateTime startDate, LocalDateTime endDate,
      List<String> judgments, List<String> regimes, String stockName) {
    StringBuilder sql = new StringBuilder(SEARCH_SQL);
    List<Object> arguments = new ArrayList<>();

    appendRange(sql, arguments, "snap.recorded_at >= ?", startDate);
    appendRange(sql, arguments, "snap.recorded_at <= ?", endDate);
    appendIn(sql, arguments, "snap.judgment", judgments);
    appendIn(sql, arguments, "snap.captured_regime", regimes);

    if (hasText(stockName)) {
      sql.append(" and s.name like ?");
      arguments.add("%" + stockName + "%");
      // 종목명 검색어가 있으면 매칭 정확도(접두 일치 > 부분 일치) 순으로, 동점이면 최신순
      sql.append(" order by case when s.name like ? then 1 when s.name like ? then 2 else 3 end asc,")
          .append(" snap.recorded_at desc");
      arguments.add(stockName + "%");
      arguments.add("%" + stockName + "%");
    } else {
      sql.append(" order by snap.recorded_at desc");
    }

    return jdbcTemplate.query(sql.toString(), LIST_MAPPER, arguments.toArray());
  }

  private void appendRange(StringBuilder sql, List<Object> arguments, String condition, LocalDateTime value) {
    if (value == null) {
      return;
    }
    sql.append(" and ").append(condition);
    arguments.add(value);
  }

  private void appendIn(StringBuilder sql, List<Object> arguments, String column, List<String> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    sql.append(" and ").append(column)
        .append(" in (").append(String.join(", ", Collections.nCopies(values.size(), "?"))).append(")");
    arguments.addAll(values);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
