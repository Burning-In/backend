package com.momentum.application;

import com.momentum.domain.relativestrength.Kospi;
import com.momentum.domain.relativestrength.KospiRelativeStrengthService;
import com.momentum.domain.relativestrength.KospiRepository;
import com.momentum.infrastructure.LsKospiProvider;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRsFacade {

  private final LsKospiProvider lsKospiProvider;
  private final KospiRepository kospiRepository;
  private final KospiRelativeStrengthService kospiRelativeStrengthService;

  public void calculate(LocalDate today) {
    long afterMarketKospi = lsKospiProvider.getAfterMarketKospi(today);
    kospiRepository.save(new Kospi(afterMarketKospi, today));

    kospiRelativeStrengthService.create(today);
  }
}
