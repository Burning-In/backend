package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.indicator.price.QStockBase.stockBase;

import com.momentum.domain.entity.indicator.price.StockBase;
import com.momentum.domain.respository.StockBaseRepository;
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
  public List<StockBase> saveAll(List<StockBase> stockBases) {
    return stockBaseJpaRepository.saveAll(stockBases);
  }

  @Override
  public Optional<StockBase> findCurrentBaseWithLines(Long stockId) {
    StockBase result = queryFactory
        .selectFrom(stockBase)
        .leftJoin(stockBase.stockBaseLines).fetchJoin()
        .where(
            stockBase.stock.id.eq(stockId),
            stockBase.deletedAt.isNull()
        )
        .orderBy(stockBase.createdAt.desc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<StockBase> findPreviousBase(Long stockId, Instant currentBaseCreatedAt) {
    StockBase result = queryFactory
        .selectFrom(stockBase)
        .where(
            stockBase.stock.id.eq(stockId),
            stockBase.createdAt.lt(ZonedDateTime.from(currentBaseCreatedAt)),
            stockBase.deletedAt.isNull()
        )
        .orderBy(stockBase.createdAt.desc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
