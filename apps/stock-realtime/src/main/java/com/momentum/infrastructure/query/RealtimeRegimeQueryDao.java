package com.momentum.infrastructure.query;

import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RealtimeRegimeQueryDao {

  private static final String FIND_SQL = """
      select s.stock_regime, s.stock_trend, b.is_vcp,
             support.price as support_price,
             resistance.price as resistance_price,
             (select p.price
                from stock_anchor_point p
               where p.stock_id = s.id
                 and p.deleted_at is null
               order by p.trade_date desc, p.id desc
               limit 1) as last_anchor_price
        from stock s
        left join stock_base b
          on b.id = (select b2.id
                       from stock_base b2
                      where b2.stock_id = s.id
                        and b2.deleted_at is null
                        and b2.stock_base_kind = 'BASE'
                      order by b2.started_at desc, b2.id desc
                      limit 1)
        left join stock_base_line support on support.id = b.lowest_support_line_id
        left join stock_base_line resistance on resistance.id = b.highest_resistance_line_id
       where s.code = ?
         and s.deleted_at is null
      """;

  private static final String UPDATE_REGIME_SQL = """
      update stock
         set stock_regime = ?, updated_at = ?
       where code = ?
         and stock_regime = ?
         and deleted_at is null
      """;

  private final JdbcTemplate jdbcTemplate;

  public Optional<RealtimeRegimeRow> findRegimeSource(String stockCode) {
    return jdbcTemplate.query(FIND_SQL, (rs, rowNum) -> new RealtimeRegimeRow(
        rs.getString("stock_regime"),
        rs.getString("stock_trend"),
        nullableLong(rs.getObject("support_price")),
        nullableLong(rs.getObject("resistance_price")),
        rs.getBoolean("is_vcp"),
        nullableLong(rs.getObject("last_anchor_price"))), stockCode).stream().findFirst();
  }

  /**
   * 레짐이 아직 {@code previousRegime}일 때만 갱신한다. 갱신된 행이 있으면 true.
   */
  public boolean updateRegime(String stockCode, String previousRegime, String newRegime) {
    int updated = jdbcTemplate.update(UPDATE_REGIME_SQL,
        newRegime, ZonedDateTime.now().toOffsetDateTime(), stockCode, previousRegime);
    return updated > 0;
  }

  private static Long nullableLong(Object value) {
    if (value == null) {
      return null;
    }
    return ((Number) value).longValue();
  }
}
