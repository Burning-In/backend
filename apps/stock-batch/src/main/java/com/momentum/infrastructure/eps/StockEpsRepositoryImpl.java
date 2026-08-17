package com.momentum.infrastructure.eps;

import static com.momentum.domain.eps.QStockEps.stockEps;

import com.momentum.domain.eps.StockEps;
import com.momentum.domain.eps.StockEpsRepository;
import com.momentum.domain.stock.Stock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockEpsRepositoryImpl implements StockEpsRepository {

  private final StockJpaEpsRepository stockJpaEpsRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Optional<StockEps> findOneYearAgo(Stock stock, LocalDate quarter) {
    StockEps result = jpaQueryFactory.selectFrom(stockEps)
        .where(
            stockEps.deletedAt.isNull(),
            stockEps.stock.eq(stock),
            stockEps.quarter.eq(quarter.minusYears(1))
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<StockEps> saveAll(List<StockEps> stockEps) {
    return stockJpaEpsRepository.saveAll(stockEps);
  }

}
