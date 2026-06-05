package com.momentum.infrastructure.score;

import static com.momentum.domain.score.QStockRankScore.stockRankScore;

import com.momentum.domain.score.QStockRankScore;
import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRankScoreRepositoryImpl implements StockRankScoreRepository {

  private final StockRankScoreJpaRepository stockRankScoreJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public StockRankScore save(StockRankScore stockRankScore) {
    return stockRankScoreJpaRepository.save(stockRankScore);
  }

  @Override
  public Optional<StockRankScore> findLatestByStock(Stock stock) {
    return stockRankScoreJpaRepository.findLatestByStock(stock);
  }

  @Override
  public List<StockRankScore> findAllByBaseDate(LocalDate baseDate) {
    return stockRankScoreJpaRepository.findAllByBaseDateAndDeletedAtIsNull(baseDate);
  }

  @Override
  public List<StockRankScore> findLastStockRankScore(StockRegime regime, LocalDate tradeDate,
      long limit) {
    QStockRankScore sub = new QStockRankScore("sub");

    return jpaQueryFactory.selectFrom(stockRankScore)
        .where(
            stockRankScore.deletedAt.isNull(),
            stockRankScore.stock.stockRegime.eq(regime),
            stockRankScore.baseDate.eq(
                JPAExpressions.select(sub.baseDate.max())
                    .from(sub)
                    .where(
                        sub.stock.eq(stockRankScore.stock),
                        sub.deletedAt.isNull(),
                        sub.baseDate.loe(tradeDate)
                    )
            )
        )
        .orderBy(
            stockRankScore.momentum.value.desc(),
            stockRankScore.fipScore.fip.asc()
        )
        .limit(limit)
        .fetch();
  }
}
