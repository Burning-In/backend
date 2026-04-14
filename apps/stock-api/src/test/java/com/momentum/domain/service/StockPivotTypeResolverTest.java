package com.momentum.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockRegime;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockBaseVolatility.StockPivotType;
import com.momentum.domain.entity.indicator.price.StockPivot;
import com.momentum.domain.respository.StockPivotRepository;
import com.momentum.domain.respository.StockRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class StockPivotTypeResolverTest {

  @Autowired private StockPivotTypeResolver stockPivotTypeResolver;
  @Autowired private StockPivotRepository stockPivotRepository;
  @Autowired private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.UNDEFIED, StockTrend.OTHER));
  }

  @Test
  @DisplayName("피벗이 3개 미만이면 타입 미확정 (UNDEFINED 유지)")
  void resolveType_lessThan3Pivots() {
    // given
    stockPivotRepository.save(StockPivot.create(10000L, LocalDate.of(2024, 1, 1), stock));
    StockPivot current = stockPivotRepository.save(StockPivot.create(11000L, LocalDate.of(2024, 1, 2), stock));

    // when
    stockPivotTypeResolver.resolveType(current);

    // then
    assertThat(current.getStockPivotType()).isEqualTo(StockPivotType.UNDEFINED);
  }

  @Test
  @DisplayName("middle이 양쪽보다 높으면 PIVOT_HIGH")
  void resolveType_pivotHigh() {
    // given
    // oldest(10000) → middle(15000) → current(12000)
    stockPivotRepository.save(StockPivot.create(10000L, LocalDate.of(2024, 1, 1), stock));
    StockPivot middle = stockPivotRepository.save(StockPivot.create(15000L, LocalDate.of(2024, 1, 2), stock));
    StockPivot current = stockPivotRepository.save(StockPivot.create(12000L, LocalDate.of(2024, 1, 3), stock));

    // when
    stockPivotTypeResolver.resolveType(current);

    // then
    assertThat(middle.getStockPivotType()).isEqualTo(StockPivotType.PIVOT_HIGH);
  }

  @Test
  @DisplayName("middle이 양쪽보다 낮으면 PIVOT_LOW")
  void resolveType_pivotLow() {
    // given
    // oldest(15000) → middle(8000) → current(12000)
    stockPivotRepository.save(StockPivot.create(15000L, LocalDate.of(2024, 1, 1), stock));
    StockPivot middle = stockPivotRepository.save(StockPivot.create(8000L, LocalDate.of(2024, 1, 2), stock));
    StockPivot current = stockPivotRepository.save(StockPivot.create(12000L, LocalDate.of(2024, 1, 3), stock));

    // when
    stockPivotTypeResolver.resolveType(current);

    // then
    assertThat(middle.getStockPivotType()).isEqualTo(StockPivotType.PIVOT_LOW);
  }

  @Test
  @DisplayName("판단 불가 케이스 (계속 상승) → UNDEFINED 유지")
  void resolveType_undetermined() {
    // given
    // oldest(10000) → middle(11000) → current(12000) 계속 상승
    stockPivotRepository.save(StockPivot.create(10000L, LocalDate.of(2024, 1, 1), stock));
    StockPivot middle = stockPivotRepository.save(StockPivot.create(11000L, LocalDate.of(2024, 1, 2), stock));
    StockPivot current = stockPivotRepository.save(StockPivot.create(12000L, LocalDate.of(2024, 1, 3), stock));

    // when
    stockPivotTypeResolver.resolveType(current);

    // then
    assertThat(middle.getStockPivotType()).isEqualTo(StockPivotType.UNDEFINED);
  }
}
