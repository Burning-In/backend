package com.momentum.infrastructure.pricepoint;

import static com.momentum.domain.pricepoint.entity.QStockPricePoint.stockPricePoint;

import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.infrastructure.pricepoint.dto.RecentPricePoints;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPricePointRepositoryImpl implements StockPricePointRepository {

  private final StockPricePointJpaRepository stockPricePointJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public StockPricePoint save(StockPricePoint stockPricePoint) {
    return stockPricePointJpaRepository.save(stockPricePoint);
  }

  @Override
  public Optional<StockPricePoint> findLastStockPricePoint(Stock stock) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stock.eq(stock),
            stockPricePoint.deletedAt.isNull())
        .orderBy(stockPricePoint.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockPricePoint> findLatestByStock(Stock stock) {
    StockPricePoint result = jpaQueryFactory
        .selectFrom(stockPricePoint)
        .where(stockPricePoint.stock.eq(stock))
        .orderBy(stockPricePoint.tradeDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<RecentPricePoints> findRecentPricePoints(Long stockId) {
    List<StockPricePoint> points = jpaQueryFactory
        .selectFrom(stockPricePoint)
        .where(stockPricePoint.stock.id.eq(stockId))
        .orderBy(stockPricePoint.tradeDate.desc())
        .limit(4)
        .fetch();

    if (points.size() < 3) {
      return Optional.empty();
    }

    StockPricePoint point0 = null;
    if (points.size() == 4) {
      point0 = points.get(3);
    }

    return Optional.of(new RecentPricePoints(
        point0,
        points.get(2),
        points.get(1),
        points.get(0)
    ));
  }

  @Override
  public Optional<StockPricePoint> findHighPricePoint(StockBase currentBase, long overPrice) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.type.eq(StockPricePointType.HIGH),
            stockPricePoint.createdAt.gt(currentBase.getCreatedAt()),
            stockPricePoint.price.price.gt(overPrice)
        )
        .orderBy(stockPricePoint.tradeDate.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockPricePoint> findLowPricePoint(StockBase currentBase, long lowerPrice) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.type.eq(StockPricePointType.LOW),
            stockPricePoint.createdAt.gt(currentBase.getCreatedAt()),
            stockPricePoint.price.price.lt(lowerPrice)
        )
        .orderBy(stockPricePoint.tradeDate.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockPricePoint> findLastPricePointWithoutBase(StockPricePointType stockPricePointType) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.type.eq(stockPricePointType)
        )
        .orderBy(stockPricePoint.tradeDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockPricePoint> findUnassignedPointsSinceBase(StockBase currentBase) {
    return jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.stock.id.eq(currentBase.getStock().getId()),
            stockPricePoint.createdAt.goe(currentBase.getCreatedAt()))
        .fetch();
  }

  @Override
  public List<StockPricePoint> saveAll(List<StockPricePoint> stockPricePoints) {
    return stockPricePointJpaRepository.saveAll(stockPricePoints);
  }
}
