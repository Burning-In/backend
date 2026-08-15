package com.momentum.infrastructure.base;

import static com.momentum.domain.base.entity.QStockBase.stockBase;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.base.entity.StockBaseKind;
import com.momentum.domain.stock.Stock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBaseRepositoryImpl implements StockBaseRepository {

  private final StockBaseJpaRepository stockBaseJpaRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public StockBase save(StockBase stockBase) {
    return stockBaseJpaRepository.save(stockBase);
  }

  @Override
  public Optional<StockBase> findCurrentBaseWithLines(Stock stock) {
    StockBase result = queryFactory
        .selectFrom(stockBase)
        .leftJoin(stockBase.stockBaseLines).fetchJoin()
        .where(
            stockBase.stock.id.eq(stock.getId()),
            stockBase.stockBaseKind.eq(StockBaseKind.BASE),
            stockBase.deletedAt.isNull()
        )
        .orderBy(stockBase.createdAt.desc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockBase> findAllByStockOrderByCreatedAt(Stock stock) {
    return queryFactory
        .selectFrom(stockBase)
        .where(
            stockBase.stock.id.eq(stock.getId()),
            stockBase.deletedAt.isNull()
        )
        .orderBy(stockBase.createdAt.asc())
        .fetch();
  }

  @Override
  public Optional<StockBase> findPreviousBase(Stock stock, Instant currentBaseCreatedAt) {
    StockBase result = queryFactory
        .selectFrom(stockBase)
        .where(
            stockBase.stock.id.eq(stock.getId()),
            stockBase.createdAt.lt(ZonedDateTime.from(currentBaseCreatedAt)),
            stockBase.deletedAt.isNull()
        )
        .orderBy(stockBase.createdAt.desc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockBase> findWithAnchorPointsById(Long baseId) {
    StockBase result = queryFactory
        .selectFrom(stockBase)
        .leftJoin(stockBase.stockBaseLines).fetchJoin()
        .where(
            stockBase.id.eq(baseId),
            stockBase.deletedAt.isNull()
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
