package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.analysis.base.QStockBaseLine.stockBaseLine;

import com.momentum.domain.entity.analysis.base.StockBaseLine;
import com.momentum.domain.entity.analysis.base.StockBaseLineType;
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
  public Optional<StockBaseLine> findTopResistanceInRange(Long stockId, long highPivotPointClosePrice, double thresholdPercent) {
    double lowerBound = highPivotPointClosePrice * ((100 - thresholdPercent) / 100);
    double upperBound = highPivotPointClosePrice * ((100 + thresholdPercent) / 100);

    StockBaseLine result = jpaQueryFactory
        .selectFrom(stockBaseLine)
        .where(
            stockBaseLine.stockBase.stock.id.eq(stockId),
            stockBaseLine.lineType.eq(StockBaseLineType.RESISTANCE),
            stockBaseLine.price.gt(lowerBound).and(stockBaseLine.price.lt(upperBound))
        )
        .orderBy(stockBaseLine.price.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockBaseLine> findLowestSupportInRange(Long stockId, long lowPivotPointClosePrice,
      double thresholdPercent) {
    double lowerBound = lowPivotPointClosePrice * ((100 - thresholdPercent) / 100);
    double upperBound = lowPivotPointClosePrice * ((100 + thresholdPercent) / 100);

    StockBaseLine result = jpaQueryFactory
        .selectFrom(stockBaseLine)
        .where(
            stockBaseLine.stockBase.stock.id.eq(stockId),
            stockBaseLine.lineType.eq(StockBaseLineType.SUPPORT),
            stockBaseLine.price.gt(lowerBound).and(stockBaseLine.price.lt(upperBound))
        )
        .orderBy(stockBaseLine.price.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public StockBaseLine save(StockBaseLine stockBaseLine) {
    return stockLineJpaRepository.save(stockBaseLine);
  }

  @Override
  public Optional<StockBaseLine> findLastResistance(Long stockId) {
    StockBaseLine result = jpaQueryFactory.selectFrom(stockBaseLine)
        .where(
            stockBaseLine.stockBase.stock.id.eq(stockId),
            stockBaseLine.lineType.eq(StockBaseLineType.RESISTANCE)
        )
        .orderBy(stockBaseLine.createdAt.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }
}
