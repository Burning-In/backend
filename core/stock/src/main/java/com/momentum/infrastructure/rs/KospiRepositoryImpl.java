package com.momentum.infrastructure.rs;

import static com.momentum.domain.relativestrength.QKospi.kospi;

import com.momentum.domain.relativestrength.Kospi;
import com.momentum.domain.relativestrength.KospiRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KospiRepositoryImpl implements KospiRepository {

  private final KospiJpaRepository kospiJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Kospi save(Kospi kospi) {
    return kospiJpaRepository.save(kospi);
  }

  @Override
  public List<Kospi> saveAll(List<Kospi> indexes) {
    return kospiJpaRepository.saveAll(indexes);
  }

  @Override
  public Optional<Kospi> findByDate(LocalDate date) {
    Kospi result = jpaQueryFactory.selectFrom(kospi)
        .where(
            kospi.deletedAt.isNull(),
            kospi.recordDate.eq(date)
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Optional<Kospi> findRecentKospi(LocalDate date) {
    Kospi result = jpaQueryFactory.selectFrom(kospi)
        .where(
            kospi.deletedAt.isNull(),
            kospi.recordDate.loe(date)
        )
        .orderBy(kospi.recordDate.desc())
        .fetchFirst();

    return Optional.ofNullable(result);
  }
}
