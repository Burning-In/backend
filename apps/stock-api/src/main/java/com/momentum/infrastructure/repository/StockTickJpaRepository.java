package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.stock.StockTick;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTickJpaRepository extends JpaRepository<StockTick, Long> {

  /** 당일 첫 번째 틱 */
  Optional<StockTick> findFirstByCreatedAtBetweenOrderByCreatedAtAsc(
      ZonedDateTime start, ZonedDateTime end);

  /**
   * 당일 특정 가격 구간 틱들의 평균 체결강도.
   * price BETWEEN fromPrice AND toPrice 조건으로 필터링한다.
   */
  @Query("SELECT AVG(t.contractPower) FROM StockTick t " +
      "WHERE t.createdAt >= :start AND t.createdAt < :end " +
      "AND t.price >= :fromPrice AND t.price <= :toPrice")
  Double findAverageContractPowerByDateAndPriceRange(
      @Param("start") ZonedDateTime start,
      @Param("end") ZonedDateTime end,
      @Param("fromPrice") long fromPrice,
      @Param("toPrice") long toPrice);
}
