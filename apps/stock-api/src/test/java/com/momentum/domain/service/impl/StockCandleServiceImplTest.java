package com.momentum.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.service.StockCandleService;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoResponse;
import com.momentum.infrastructure.lsinvestment.dto.StockChartInfoResponse.CandleResponse;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class StockCandleServiceImplTest {

  @Autowired
  private StockCandleService stockCandleService;

  @Autowired
  private StockRepository stockRepository;

  @Transactional
  @Test
  void create_shouldSaveCandles() {
    // given
    String stockCode = "005930";

    Stock stock = new Stock("삼성전자", stockCode);
    stockRepository.save(stock);
    StockChartInfoResponse response = new StockChartInfoResponse(
        List.of(
            new CandleResponse("20240101", 100L, 110L, 90L, 105L, 100, 100, "1"),
            new CandleResponse("20240102", 105L, 115L, 95L, 110L, 100, 100, "1")
        )
    );

    // when
    List<StockCandle> result = stockCandleService.create(stockCode, response);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(
        StockCandle::getTradeDate,
            s -> s.getStock().getCode()
        )
        .containsExactlyInAnyOrder(
            Tuple.tuple("20240101", stockCode),
            Tuple.tuple("20240102", stockCode)
        );
  }
}
