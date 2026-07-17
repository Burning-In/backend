package com.momentum.domain.relativestrength;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KospiRepository {

  Kospi save(Kospi kospi);

  List<Kospi> saveAll(List<Kospi> indexes);

  Optional<Kospi> findByDate(LocalDate date);

  Optional<Kospi> findRecentKospi(LocalDate date);
}
