package com.momentum.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.infrastructure.dto.FinancialRatioResponse.Output;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
class EpsProviderTest {

  @Autowired
  private EpsProvider epsProvider;

  @Test
  void 삼성전자_분기별_EPS를_가져온다() {
    List<Output> result = epsProvider.getQuarterlyEps("005930");

    result.forEach(o -> System.out.println(o.stacYymm() + " | EPS: " + o.eps()));
    assertThat(result).isNotEmpty();
  }
}
