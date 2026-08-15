package com.momentum.infrastructure.anchorpoint;

import static com.momentum.domain.anchorpoint.entity.QStockAnchorPoint.stockAnchorPoint;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.dto.RecentAnchorPoints;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
import com.momentum.domain.stock.Stock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockAnchorPointRepositoryImpl implements StockAnchorPointRepository {

  private final StockAnchorPointJpaRepository stockAnchorPointJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public StockAnchorPoint save(StockAnchorPoint stockAnchorPoint) {
    return stockAnchorPointJpaRepository.save(stockAnchorPoint);
  }

  @Override
  public Optional<StockAnchorPoint> findLastStockAnchorPoint(Stock stock) {
    StockAnchorPoint result = jpaQueryFactory.selectFrom(stockAnchorPoint)
        .where(
            stockAnchorPoint.stock.eq(stock),
            stockAnchorPoint.deletedAt.isNull())
        .orderBy(stockAnchorPoint.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockAnchorPoint> findLatestByStock(Stock stock) {
    StockAnchorPoint result = jpaQueryFactory
        .selectFrom(stockAnchorPoint)
        .where(stockAnchorPoint.stock.eq(stock))
        .orderBy(stockAnchorPoint.tradeDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<RecentAnchorPoints> findRecentAnchorPoints(Long stockId) {
    List<StockAnchorPoint> points = jpaQueryFactory
        .selectFrom(stockAnchorPoint)
        .where(stockAnchorPoint.stock.id.eq(stockId))
        .orderBy(stockAnchorPoint.tradeDate.desc())
        .limit(4)
        .fetch();

    if (points.size() < 3) {
      return Optional.empty();
    }

    StockAnchorPoint oldest = null;
    if (points.size() == 4) {
      oldest = points.get(3);
    }

    return Optional.of(new RecentAnchorPoints(
        oldest,
        points.get(2),
        points.get(1),
        points.get(0)
    ));
  }

  @Override
  public Optional<StockAnchorPoint> findHighAnchorPoint(StockBase currentBase, long overPrice) {
    StockAnchorPoint result = jpaQueryFactory.selectFrom(stockAnchorPoint)
        .where(
            stockAnchorPoint.stockBase.isNull(),
            stockAnchorPoint.type.eq(StockAnchorPointType.HIGH),
            stockAnchorPoint.createdAt.gt(currentBase.getCreatedAt()),
            stockAnchorPoint.price.price.gt(overPrice)
        )
        .orderBy(stockAnchorPoint.tradeDate.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockAnchorPoint> findLowAnchorPoint(StockBase currentBase, long lowerPrice) {
    StockAnchorPoint result = jpaQueryFactory.selectFrom(stockAnchorPoint)
        .where(
            stockAnchorPoint.stockBase.isNull(),
            stockAnchorPoint.type.eq(StockAnchorPointType.LOW),
            stockAnchorPoint.createdAt.gt(currentBase.getCreatedAt()),
            stockAnchorPoint.price.price.lt(lowerPrice)
        )
        .orderBy(stockAnchorPoint.tradeDate.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockAnchorPoint> findLastAnchorPointWithoutBase(StockAnchorPointType stockAnchorPointType) {
    StockAnchorPoint result = jpaQueryFactory.selectFrom(stockAnchorPoint)
        .where(
            stockAnchorPoint.stockBase.isNull(),
            stockAnchorPoint.type.eq(stockAnchorPointType)
        )
        .orderBy(stockAnchorPoint.tradeDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockAnchorPoint> findUnassignedPointsSinceBase(StockBase currentBase) {
    return jpaQueryFactory.selectFrom(stockAnchorPoint)
        .where(
            stockAnchorPoint.stockBase.isNull(),
            stockAnchorPoint.stock.id.eq(currentBase.getStock().getId()),
            stockAnchorPoint.createdAt.goe(currentBase.getCreatedAt()))
        .fetch();
  }

  @Override
  public List<StockAnchorPoint> saveAll(List<StockAnchorPoint> stockAnchorPoints) {
    return stockAnchorPointJpaRepository.saveAll(stockAnchorPoints);
  }
}
