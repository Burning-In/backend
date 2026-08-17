package com.momentum.infrastructure.query;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockMetaQueryDao {

  private static final String SEARCH_SQL = """
      select s.code, s.name, s.stock_regime, s.stock_trend
        from stock s
       where s.deleted_at is null
         and (s.code like ? or s.name like ?)
       order by case when s.name like ? then 1
                     when s.name like ? then 2
                     when s.code like ? then 3
                     else 4 end asc,
                s.created_at asc
      """;

  private static final String FIND_BY_CODE_SQL = """
      select s.code, s.name, s.stock_regime, s.stock_trend
        from stock s
       where s.code = ?
         and s.deleted_at is null
      """;

  private static final String FIND_IDENTITY_BY_CODE_SQL = """
      select s.id, s.code, s.name
        from stock s
       where s.code = ?
         and s.deleted_at is null
      """;

  private static final String FIND_IDENTITIES_BY_IDS_SQL = """
      select s.id, s.code, s.name
        from stock s
       where s.id in (%s)
      """;

  private static final RowMapper<StockIdentityRow> IDENTITY_MAPPER = (rs, rowNum) -> new StockIdentityRow(
      rs.getLong("id"),
      rs.getString("code"),
      rs.getString("name"));

  private static final RowMapper<StockMetaRow> META_MAPPER = (rs, rowNum) -> new StockMetaRow(
      rs.getString("code"),
      rs.getString("name"),
      rs.getString("stock_regime"),
      rs.getString("stock_trend"));

  private final JdbcTemplate jdbcTemplate;

  public List<StockMetaRow> search(String query) {
    String prefix = query + "%";
    String contains = "%" + query + "%";
    return jdbcTemplate.query(SEARCH_SQL, META_MAPPER, contains, contains, prefix, contains, prefix);
  }

  public Optional<StockMetaRow> findByCode(String stockCode) {
    return jdbcTemplate.query(FIND_BY_CODE_SQL, META_MAPPER, stockCode).stream().findFirst();
  }

  public Optional<StockIdentityRow> findIdentityByCode(String stockCode) {
    return jdbcTemplate.query(FIND_IDENTITY_BY_CODE_SQL, IDENTITY_MAPPER, stockCode).stream().findFirst();
  }

  public Map<Long, StockIdentityRow> findIdentitiesByIds(List<Long> stockIds) {
    if (stockIds.isEmpty()) {
      return Map.of();
    }
    String placeholders = String.join(", ", Collections.nCopies(stockIds.size(), "?"));
    return jdbcTemplate.query(FIND_IDENTITIES_BY_IDS_SQL.formatted(placeholders), IDENTITY_MAPPER,
            stockIds.toArray())
        .stream()
        .collect(Collectors.toMap(StockIdentityRow::stockId, Function.identity()));
  }
}
