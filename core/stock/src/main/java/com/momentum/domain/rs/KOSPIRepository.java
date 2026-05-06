package com.momentum.domain.rs;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KOSPIRepository {

  KOSPI save(KOSPI kospi);

  List<KOSPI> saveAll(List<KOSPI> indexes);

  Optional<KOSPI> findByDate(LocalDate date);

  Optional<KOSPI> findRecentKOSPI(LocalDate date);
}
