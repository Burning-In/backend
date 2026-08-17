package com.momentum.infrastructure.anchorpoint;

import static com.momentum.domain.anchorpoint.entity.QStockAnchorPointCalculation.stockAnchorPointCalculation;

import com.momentum.domain.anchorpoint.StockAnchorPointCalculationRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointCalculation;
import com.momentum.domain.stock.Stock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAnchorPointCalculationRepositoryImpl implements StockAnchorPointCalculationRepository {

  private final StockAnchorPointCalculationJpaRepository stockPivotCalculateHistoryRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<StockAnchorPointCalculation> findLastCalculationHistory(Stock stock) {
    StockAnchorPointCalculation result = jpaQueryFactory.selectFrom(stockAnchorPointCalculation)
        .where(stockAnchorPointCalculation.stockAnchorPoint.stock.eq(stock))
        .orderBy(stockAnchorPointCalculation.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public StockAnchorPointCalculation save(StockAnchorPointCalculation stockPivotCalculateHistory) {
    return stockPivotCalculateHistoryRepository.save(stockPivotCalculateHistory);
  }
}
