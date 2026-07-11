package com.momentum.infrastructure.stocktick;

import com.momentum.domain.stock.TrackedStock;
import com.momentum.domain.stocktick.StockTick;
import com.momentum.domain.stocktick.StockTickRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockTickRepositoryImpl implements StockTickRepository {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final StockTickJpaRepository stockTickJpaRepository;

  @Override
  public StockTick save(StockTick stockTick) {
    return stockTickJpaRepository.save(stockTick);
  }

  @Override
  public Optional<StockTick> findDailyFirst(Instant now) {
    ZonedDateTime[] range = dailyRange(now);
    return stockTickJpaRepository
        .findFirstByCreatedAtBetweenOrderByCreatedAtAsc(range[0], range[1]);
  }

  @Override
  public Optional<StockTick> findLatestTick(TrackedStock stockName, LocalDateTime at) {
    ZonedDateTime atKst = at.atZone(KST);
    return stockTickJpaRepository
        .findFirstByStockNameAndCreatedAtLessThanEqualOrderByCreatedAtDescIdDesc(stockName, atKst);
  }

  @Override
  public Double averageDailyOrderFlow(Instant now, long fromPrice, long toPrice) {
    ZonedDateTime[] range = dailyRange(now);
    return stockTickJpaRepository
        .findAverageContractPowerByDateAndPriceRange(range[0], range[1], fromPrice, toPrice);
  }

  /**
   * now 기준 당일 시작(00:00:00) ~ 다음 날 시작(00:00:00) KST ZonedDateTime 배열 반환
   */
  private ZonedDateTime[] dailyRange(Instant now) {
    LocalDate today = LocalDate.ofInstant(now, KST);
    ZonedDateTime start = today.atStartOfDay(KST);
    ZonedDateTime end = today.plusDays(1).atStartOfDay(KST);
    return new ZonedDateTime[]{start, end};
  }
}
