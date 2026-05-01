package com.momentum.infrastructure.pricepoint;

import static com.momentum.domain.pricepoint.entity.QStockPricePointCalculation.stockPricePointCalculation;

import com.momentum.domain.pricepoint.StockPricePointCalculationRepository;
import com.momentum.domain.pricepoint.entity.StockPricePointCalculation;
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
  public Optional<StockPricePointCalculation> findLastCalculationHistory(Stock stock) {
    StockPricePointCalculation result = jpaQueryFactory.selectFrom(stockPricePointCalculation)
        .where(stockPricePointCalculation.stockPricePoint.stock.eq(stock))
        .orderBy(stockPricePointCalculation.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public StockPricePointCalculation save(StockPricePointCalculation stockPivotCalculateHistory) {
    return stockPivotCalculateHistoryRepository.save(stockPivotCalculateHistory);
  }
}
