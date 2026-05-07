package com.momentum.application;

import com.momentum.domain.rs.KOSPI;
import com.momentum.domain.rs.KOSPIRelativeStrengthService;
import com.momentum.domain.rs.KOSPIRepository;
import com.momentum.infrastructure.LsKospiProvider;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockRsFacade {

  private final LsKospiProvider lsKospiProvider;
  private final KOSPIRepository kOSPIRepository;
  private final KOSPIRelativeStrengthService kOSPIRelativeStrengthService;

  public void calculate(LocalDate today) {
    long afterMarketKospi = lsKospiProvider.getAfterMarketKospi();
    kOSPIRepository.save(new KOSPI(afterMarketKospi, today));

    kOSPIRelativeStrengthService.create(today);
  }
}
