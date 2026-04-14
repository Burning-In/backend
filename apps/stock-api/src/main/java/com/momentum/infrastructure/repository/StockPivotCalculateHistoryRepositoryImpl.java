package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.indicator.price.QStockPivotCalculateHistory.stockPivotCalculateHistory;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockPivotCalculateHistory;
import com.momentum.domain.respository.StockPivotCalculateHistoryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPivotCalculateHistoryRepositoryImpl implements StockPivotCalculateHistoryRepository {

  private final StockPivotCalculateHistoryJpaRepository stockPivotCalculateHistoryRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<StockPivotCalculateHistory> findTopCalculationHistory(Stock stock) {
    StockPivotCalculateHistory result = jpaQueryFactory.selectFrom(stockPivotCalculateHistory)
        .where(
            stockPivotCalculateHistory.stockPivot.stock.eq(stock)
        )
        .orderBy(stockPivotCalculateHistory.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public StockPivotCalculateHistory save(StockPivotCalculateHistory stockPivotCalculateHistory) {
    return stockPivotCalculateHistoryRepository.save(stockPivotCalculateHistory);
  }
}
