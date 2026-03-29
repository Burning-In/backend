package com.momentum.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.StockCode;
import com.momentum.infrastructure.dto.StockChartInfoResponse;
import com.momentum.infrastructure.dto.StockChartInfoResponse.Candle;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Disabled
@SpringBootTest
@TestPropertySource(locations = "file:../../env.properties")
class LsStockChartClientTest {

  @Autowired
  private LsStockChartClient lsStockChartClient;

  @Test
  @DisplayName("일봉 데이터를 가져옵니다.")
  void getDailyCandles() {
    // given
    String startDate = "20260327";

    // when
    StockChartInfoResponse dailyCandles = lsStockChartClient.getDailyCandles(StockCode.삼성전자_ST10.getCode(),
        1,
        startDate,
        startDate
    );

    // then
    assertThat(dailyCandles.candles())
        .extracting(Candle::closePrice)
        .containsExactlyInAnyOrder(176800L);
  }
}
