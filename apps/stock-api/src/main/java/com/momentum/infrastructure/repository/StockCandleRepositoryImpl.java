package com.momentum.infrastructure.repository;

import com.momentum.domain.entity.QStockCandle;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.respository.StockCandleRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockCandleRepositoryImpl implements StockCandleRepository {

  private final StockCandleJpaRepository stockCandleJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<StockCandle> saveAll(List<StockCandle> candles) {
    return stockCandleJpaRepository.saveAll(candles);
  }

  @Override
  public StockCandle save(StockCandle candle) {
    return stockCandleJpaRepository.save(candle);
  }

  @Override
  public Optional<StockCandle> findDailyCandle(Long stockId, LocalDate tradeDate) {
    QStockCandle stockCandle = QStockCandle.stockCandle;

    StockCandle result = jpaQueryFactory
        .selectFrom(stockCandle)
        .where(
            stockCandle.stock.id.eq(stockId),
            stockCandle.tradeDate.eq(tradeDate)
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
