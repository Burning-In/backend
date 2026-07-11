package com.momentum.domain.stocktick;

import com.momentum.domain.stock.TrackedStock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface StockTickRepository {

  StockTick save(StockTick stockTick);

  /** 당일 첫 번째 틱 (장 시작 기준점) */
  Optional<StockTick> findDailyFirst(Instant now);

  /** 특정 시점({@code at}) 이전(포함)의 가장 최근 틱. 해당 시점의 현재가 조회에 사용한다. */
  Optional<StockTick> findLatestTick(TrackedStock stockName, LocalDateTime at);
}
