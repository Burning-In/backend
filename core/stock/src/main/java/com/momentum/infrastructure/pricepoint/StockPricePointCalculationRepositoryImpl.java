package com.momentum.infrastructure.pricepoint;

import static com.momentum.domain.pricepoint.entity.QStockPivotCalculation.stockPivotCalculation;

import com.momentum.domain.pricepoint.StockPricePointCalculationRepository;
import com.momentum.domain.pricepoint.entity.StockPivotCalculation;
import com.momentum.domain.stock.Stock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointCalculationRepositoryImpl implements StockPricePointCalculationRepository {

  private final StockPricePointCalculationJpaRepository stockPivotCalculateHistoryRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<StockPivotCalculation> findTopCalculationHistory(Stock stock) {
    StockPivotCalculation result = jpaQueryFactory.selectFrom(stockPivotCalculation)
        .where(stockPivotCalculation.stockPricePoint.stock.eq(stock))
        .orderBy(stockPivotCalculation.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public StockPivotCalculation save(StockPivotCalculation stockPivotCalculateHistory) {
    return stockPivotCalculateHistoryRepository.save(stockPivotCalculateHistory);
  }
}
