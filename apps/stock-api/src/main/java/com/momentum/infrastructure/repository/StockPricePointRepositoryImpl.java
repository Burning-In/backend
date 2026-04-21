package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.indicator.price.QStockPricePoint.stockPricePoint;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPricePointType;
import com.momentum.domain.respository.StockPricePointRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.ZonedDateTime;
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
  public Optional<StockPricePoint> findTopByStockOrderByCreatedAtDesc(Stock stock) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(stockPricePoint.stock.eq(stock))
        .orderBy(stockPricePoint.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockPricePoint> findTop3ByStockOrderByCreatedAtDesc(Long stockId) {
    return jpaQueryFactory.selectFrom(stockPricePoint)
        .where(stockPricePoint.stock.id.eq(stockId))
        .orderBy(stockPricePoint.createdAt.desc())
        .limit(3)
        .fetch();
  }

  @Override
  public List<StockPricePoint> findTop4ByStockOrderByCreatedAtDesc(Long stockId) {
    return jpaQueryFactory.selectFrom(stockPricePoint)
        .where(stockPricePoint.stock.id.eq(stockId))
        .orderBy(stockPricePoint.createdAt.desc())
        .limit(4)
        .fetch();
  }

  @Override
  public Optional<StockPricePoint> findUpperPricePoint(Instant currentBaseCreatedAt, long overPrice) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.stockPricePointType.eq(StockPricePointType.PIVOT_HIGH),
            stockPricePoint.createdAt.gt(ZonedDateTime.from(currentBaseCreatedAt)),
            stockPricePoint.price.gt(overPrice)
        )
        .orderBy(stockPricePoint.tradeDate.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockPricePoint> findLineLowerPricePoint(Instant currentBaseCreatedAt, long lowerPrice) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.stockPricePointType.eq(StockPricePointType.PIVOT_LOW),
            stockPricePoint.createdAt.gt(ZonedDateTime.from(currentBaseCreatedAt)),
            stockPricePoint.price.lt(lowerPrice)
        )
        .orderBy(stockPricePoint.tradeDate.asc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockPricePoint> findPricePointNoBase(StockPricePointType stockPricePointType) {
    StockPricePoint result = jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.stockPricePointType.eq(stockPricePointType)
        )
        .orderBy(stockPricePoint.tradeDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockPricePoint> findUnassignedPointsSinceBase(Instant currentBaseCreatedAt) {
    return jpaQueryFactory.selectFrom(stockPricePoint)
        .where(
            stockPricePoint.stockBase.isNull(),
            stockPricePoint.createdAt.goe(ZonedDateTime.from(currentBaseCreatedAt)))
        .fetch();
  }

  @Override
  public List<StockPricePoint> saveAll(List<StockPricePoint> stockPricePoints) {
    return stockPricePointJpaRepository.saveAll(stockPricePoints);
  }
}
