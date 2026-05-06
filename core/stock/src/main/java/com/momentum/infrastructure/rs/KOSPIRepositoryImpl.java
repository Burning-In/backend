package com.momentum.infrastructure.rs;

import static com.momentum.domain.rs.QKOSPI.kOSPI;

import com.momentum.domain.rs.KOSPI;
import com.momentum.domain.rs.KOSPIRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KOSPIRepositoryImpl implements KOSPIRepository {

  private final KOSPIJpaRepository kospiJpaRepository;
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public KOSPI save(KOSPI kospi) {
    return kospiJpaRepository.save(kospi);
  }

  @Override
  public List<KOSPI> saveAll(List<KOSPI> indexes) {
    return kospiJpaRepository.saveAll(indexes);
  }

  @Override
  public Optional<KOSPI> findByDate(LocalDate date) {
    KOSPI kospi = jpaQueryFactory.selectFrom(kOSPI)
        .where(
            kOSPI.deletedAt.isNull(),
            kOSPI.recordDate.eq(date)
        )
        .fetchOne();

    return Optional.ofNullable(kospi);
  }

  @Override
  public Optional<KOSPI> findRecentKOSPI(LocalDate date) {
    KOSPI kospi = jpaQueryFactory.selectFrom(kOSPI)
        .where(
            kOSPI.deletedAt.isNull(),
            kOSPI.recordDate.loe(date)
        )
        .orderBy(kOSPI.recordDate.desc())
        .fetchFirst();

    return Optional.ofNullable(kospi);
  }
}
