package com.momentum.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.StockBase;
import com.momentum.domain.entity.indicator.StockLine;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockLineRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.service.StockBaseService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class StockBaseServiceImplTest {

  @Autowired
  private StockBaseService stockBaseService;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private StockLineRepository stockLineRepository;

  @Autowired
  private StockBaseRepository stockBaseRepository;

  @Test
  @DisplayName("이전 베이스가 없으면 accumulationCount = 1으로 생성된다")
  void createCandidate_noPreviousBase() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930"));
    StockLine triggerLine = stockLineRepository.save(
        StockLine.resistance(100_000L, stock)
    );

    // when
    StockBase result = stockBaseService.createCandidate(stock, triggerLine);

    // then
    assertThat(result.getAccumulationCount()).isEqualTo(1L);
    assertThat(result.getStock()).isEqualTo(stock);
  }
}
