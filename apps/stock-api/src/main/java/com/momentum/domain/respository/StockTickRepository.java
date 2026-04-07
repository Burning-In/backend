package com.momentum.domain.respository;

import com.momentum.domain.entity.StockTick;
import java.time.Instant;
import java.util.Optional;

public interface StockTickRepository {

  StockTick save(StockTick stockTick);

  /** 당일 첫 번째 틱 (장 시작 기준점) */
  Optional<StockTick> findDailyFirst(Instant now);

  /**
   * 당일 특정 가격 구간의 평균 체결강도.
   *
   * @param fromPrice 구간 시작 가격 (inclusive)
   * @param toPrice   구간 종료 가격 (inclusive)
   */
  Double averageDailyOrderFlow(Instant now, long fromPrice, long toPrice);
}
