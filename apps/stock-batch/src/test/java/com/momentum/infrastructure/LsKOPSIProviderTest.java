package com.momentum.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
class LsKOPSIProviderTest {

  @Autowired
  private LsKOPSIProvider provider;

  @Test
  void 장후_KOSPI_지수를_받아온다() {
    long kospi = provider.getAfterMarketKospi();

    System.out.println("KOSPI: " + kospi);
    assertThat(kospi).isPositive();
  }
}
