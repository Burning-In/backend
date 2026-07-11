package com.momentum.infrastructure.stocktick;

import com.momentum.domain.stock.TrackedStock;
import com.momentum.domain.stocktick.StockTick;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTickJpaRepository extends JpaRepository<StockTick, Long> {

  /** 당일 첫 번째 틱 */
  Optional<StockTick> findFirstByCreatedAtBetweenOrderByCreatedAtAsc(
      ZonedDateTime start, ZonedDateTime end);

  /** 종목의 특정 시점 이전(포함) 가장 최근 틱 (동일 시각이면 최근 적재분 우선) */
  Optional<StockTick> findFirstByStockNameAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(
      TrackedStock stockName, ZonedDateTime at);
}
