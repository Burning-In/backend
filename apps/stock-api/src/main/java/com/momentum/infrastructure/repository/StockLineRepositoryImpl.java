package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.indicator.price.QStockLine.stockLine;

import com.momentum.domain.entity.indicator.price.StockLine;
import com.momentum.domain.entity.indicator.price.StockLineType;
import com.momentum.domain.respository.StockLineRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockLineRepositoryImpl implements StockLineRepository {

  private final StockLineJpaRepository stockLineJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<StockLine> findTopResistanceInRange(Long stockId, long highPivotPointClosePrice, double thresholdPercent) {
    double lowerBound = highPivotPointClosePrice * ((100 - thresholdPercent) / 100);
    double upperBound = highPivotPointClosePrice * ((100 + thresholdPercent) / 100);

    StockLine result = jpaQueryFactory
        .selectFrom(stockLine)
        .where(
            stockLine.stock.id.eq(stockId),
            stockLine.lineType.eq(StockLineType.RESISTANCE),
            stockLine.price.gt(lowerBound).and(stockLine.price.lt(upperBound))
        )
        .orderBy(stockLine.price.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockLine> findLowestSupportInRange(Long stockId, long lowPivotPointClosePrice,
      double thresholdPercent) {
    double lowerBound = lowPivotPointClosePrice * ((100 - thresholdPercent) / 100);
    double upperBound = lowPivotPointClosePrice * ((100 + thresholdPercent) / 100);

    StockLine result = jpaQueryFactory
        .selectFrom(stockLine)
        .where(
            stockLine.stock.id.eq(stockId),
            stockLine.lineType.eq(StockLineType.SUPPORT),
            stockLine.price.gt(lowerBound).and(stockLine.price.lt(upperBound))
        )
        .orderBy(stockLine.price.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public StockLine save(StockLine stockLine) {
    return stockLineJpaRepository.save(stockLine);
  }
}
