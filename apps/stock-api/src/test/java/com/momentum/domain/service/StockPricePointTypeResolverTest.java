package com.momentum.domain.service;

import com.momentum.domain.entity.stock.Stock;
import com.momentum.domain.entity.score.StockRegime;
import com.momentum.domain.entity.stock.StockTrend;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.stock.StockRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class StockPricePointTypeResolverTest {

  @Autowired
  private StockPricePointTypeResolver stockPricePointTypeResolver;
  @Autowired
  private StockPricePointRepository stockPricePointRepository;
  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.UNDETERMINED, StockTrend.OTHER));
  }

  @Test
  @DisplayName("피벗이 3개 미만이면 타입 미확정 (UNDEFINED 유지)")
  void resolveType_lessThan3Pivots() {

  }

  @Test
  @DisplayName("middle이 양쪽보다 높으면 PIVOT_HIGH")
  void resolveType_pivotHigh() {

  }

  @Test
  @DisplayName("middle이 양쪽보다 낮으면 PIVOT_LOW")
  void resolveType_pivotLow() {

  }

  @Test
  @DisplayName("판단 불가 케이스 (계속 상승) → UNDEFINED 유지")
  void resolveType_undetermined() {

  }
}
