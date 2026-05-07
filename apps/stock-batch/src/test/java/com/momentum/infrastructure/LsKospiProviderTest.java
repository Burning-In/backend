package com.momentum.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
class LsKospiProviderTest {

  @Autowired
  private LsKospiProvider provider;

  @Test
  void 장후_KOSPI_지수를_받아온다() {
    LocalDate today = LocalDate.now();
    long kospi = provider.getAfterMarketKospi(today);

    System.out.println("KOSPI: " + kospi);
    assertThat(kospi).isPositive();
  }
}
