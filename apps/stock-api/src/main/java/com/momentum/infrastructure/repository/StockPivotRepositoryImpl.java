package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.indicator.price.QStockPivot.stockPivot;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.price.QStockPivot;
import com.momentum.domain.entity.indicator.price.StockPivot;
import com.momentum.domain.respository.StockPivotRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPivotRepositoryImpl implements StockPivotRepository {

  private final StockPivotJpaRepository stockPivotJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public StockPivot save(StockPivot stockPivot) {
    return stockPivotJpaRepository.save(stockPivot);
  }

  @Override
  public Optional<StockPivot> findTopByStockOrderByCreatedAtDesc(Stock stock) {
    StockPivot result = jpaQueryFactory.selectFrom(stockPivot)
        .where(stockPivot.stock.eq(stock))
        .orderBy(stockPivot.createdAt.desc())
        .limit(1)
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockPivot> findTop3ByStockOrderByCreatedAtDesc(Stock stock) {
    return jpaQueryFactory.selectFrom(stockPivot)
        .where(stockPivot.stock.eq(stock))
        .orderBy(stockPivot.createdAt.desc())
        .limit(3)
        .fetch();
  }
}
