package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.indicator.QStockBase.stockBase;

import com.momentum.domain.entity.indicator.StockBase;
import com.momentum.domain.entity.indicator.StockBaseType;
import com.momentum.domain.respository.StockBaseRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
  public Optional<StockBase> findLastBase(Long stockId) {
    StockBase result = queryFactory
        .selectFrom(stockBase)
        .where(
            stockBase.stock.id.eq(stockId),
            stockBase.stockBaseType.eq(StockBaseType.CONFIRMED)
        )
        .orderBy(stockBase.createdAt.desc())
        .limit(1)
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
