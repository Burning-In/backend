package com.momentum.infrastructure.snapshot;

import static com.momentum.domain.QStockSnapShot.stockSnapShot;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import com.momentum.domain.stock.StockRegime;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SnapshotRepositoryImpl implements SnapshotRepository {

  private final SnapshotJpaRepository snapshotJpaRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public StockSnapShot save(StockSnapShot snapshot) {
    return snapshotJpaRepository.save(snapshot);
  }

  @Override
  public Optional<StockSnapShot> findById(Long id) {
    return snapshotJpaRepository.findById(id);
  }

  @Override
  public List<StockSnapShot> findAllByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return snapshotJpaRepository.findAllById(ids);
  }

  @Override
  public List<StockSnapShot> search(LocalDateTime startDate, LocalDateTime endDate,
      List<SnapshotJudgment> judgments, List<StockRegime> regimes, String stockName) {
    var query = queryFactory.selectFrom(stockSnapShot)
        .where(
            stockSnapShot.deletedAt.isNull(),
            recordedFrom(startDate),
            recordedTo(endDate),
            judgmentIn(judgments),
            regimeIn(regimes),
            stockNameContains(stockName)
        );

    if (stockName != null && !stockName.isBlank()) {
      // 종목명 검색어가 있으면 매칭 정확도(접두 일치 > 부분 일치) 순으로, 동점이면 최신순
      query.orderBy(stockNameMatchScore(stockName).asc(), stockSnapShot.recordedAt.desc());
    } else {
      query.orderBy(stockSnapShot.recordedAt.desc());
    }

    return query.fetch();
  }

  private NumberExpression<Integer> stockNameMatchScore(String stockName) {
    return new CaseBuilder()
        .when(stockSnapShot.stock.name.like(stockName + "%")).then(1)        // 접두 일치
        .when(stockSnapShot.stock.name.like("%" + stockName + "%")).then(2)  // 부분 일치
        .otherwise(3);
  }

  private BooleanExpression recordedFrom(LocalDateTime startDate) {
    if (startDate == null) {
      return null;
    }
    return stockSnapShot.recordedAt.goe(startDate);
  }

  private BooleanExpression recordedTo(LocalDateTime endDate) {
    if (endDate == null) {
      return null;
    }
    return stockSnapShot.recordedAt.loe(endDate);
  }

  private BooleanExpression judgmentIn(List<SnapshotJudgment> judgments) {
    if (judgments == null || judgments.isEmpty()) {
      return null;
    }
    return stockSnapShot.judgment.in(judgments);
  }

  private BooleanExpression regimeIn(List<StockRegime> regimes) {
    if (regimes == null || regimes.isEmpty()) {
      return null;
    }
    return stockSnapShot.capturedRegime.in(regimes);
  }

  private BooleanExpression stockNameContains(String stockName) {
    if (stockName == null || stockName.isBlank()) {
      return null;
    }
    return stockSnapShot.stock.name.like("%" + stockName + "%");
  }
}
