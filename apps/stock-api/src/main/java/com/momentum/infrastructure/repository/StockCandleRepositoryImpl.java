package com.momentum.infrastructure.repository;

import static com.momentum.domain.entity.QStockDailyCandle.stockDailyCandle;

import com.momentum.domain.entity.QStockDailyCandle;
import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockDailyCandle;
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
  public List<StockDailyCandle> saveAll(List<StockDailyCandle> candles) {
    return stockCandleJpaRepository.saveAll(candles);
  }

  @Override
  public StockDailyCandle save(StockDailyCandle candle) {
    return stockCandleJpaRepository.save(candle);
  }

  @Override
  public Optional<StockDailyCandle> findByStockAndDate(Stock stock, LocalDate tradeDate) {
    StockDailyCandle result = jpaQueryFactory
        .selectFrom(stockDailyCandle)
        .where(
            stockDailyCandle.stock.id.eq(stock.getId()),
            stockDailyCandle.tradeDate.eq(tradeDate)
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Long findAvgVolumeByStockAndDateAfter(Stock stock, LocalDate oneYearAgo) {
    return jpaQueryFactory
        .select(stockDailyCandle.volume.avg().longValue())
        .from(stockDailyCandle)
        .where(
            stockDailyCandle.stock.id.eq(stock.getId()),
            stockDailyCandle.tradeDate.goe(oneYearAgo)
        )
        .fetchOne();
  }
}
