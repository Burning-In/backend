package com.momentum.infrastructure.stockcandle;

import static com.momentum.domain.stockcandle.QStockDailyCandle.stockDailyCandle;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stockcandle.QStockDailyCandle;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.domain.stockcandle.StockDailyCandle;
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
  public List<StockDailyCandle> saveAll(List<StockDailyCandle> candles) {
    return stockCandleJpaRepository.saveAll(candles);
  }

  @Override
  public StockDailyCandle save(StockDailyCandle candle) {
    return stockCandleJpaRepository.save(candle);
  }

  @Override
  public Optional<StockDailyCandle> findLastCandleAfterDate(Stock stock, LocalDate tradeDate) {
    StockDailyCandle result = jpaQueryFactory
        .selectFrom(stockDailyCandle)
        .where(
            stockDailyCandle.stock.id.eq(stock.getId()),
            stockDailyCandle.tradeDate.lt(tradeDate)
        )
        .orderBy(stockDailyCandle.tradeDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }

  @Override
  public Long averageVolume(Stock stock, LocalDate from, LocalDate to) {
    return jpaQueryFactory
        .select(stockDailyCandle.volume.avg().longValue())
        .from(stockDailyCandle)
        .where(
            stockDailyCandle.deletedAt.isNull(),
            stockDailyCandle.stock.id.eq(stock.getId()),
            stockDailyCandle.tradeDate.goe(from),
            stockDailyCandle.tradeDate.loe(to)
        )
        .fetchOne();
  }

  @Override
  public List<StockDailyCandle> findRecentCandles(Long stockId, LocalDate baseDate, int limit) {
    return jpaQueryFactory
        .selectFrom(stockDailyCandle)
        .where(
            stockDailyCandle.stock.id.eq(stockId),
            stockDailyCandle.tradeDate.loe(baseDate)
        )
        .orderBy(stockDailyCandle.tradeDate.desc())
        .limit(limit)
        .fetch();
  }
}
