package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.rs.Kospi;
import com.momentum.domain.rs.KospiRelativeStrength;
import com.momentum.domain.rs.KospiRelativeStrengthRepository;
import com.momentum.domain.rs.KospiRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.RsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RsInsightServiceTest {

  @Autowired RsInsightService rsInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired KospiRepository kospiRepository;
  @Autowired KospiRelativeStrengthRepository kospiRelativeStrengthRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("RS 점수 반환")
  void returnsRsScore() {
    Kospi kospi = kospiRepository.save(new Kospi(2500L, LocalDate.now()));
    kospiRelativeStrengthRepository.saveAll(List.of(new KospiRelativeStrength(85, stock, kospi)));

    RsResponse result = rsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.rsValue()).isEqualByComparingTo(new BigDecimal("85"));
    assertThat(result.percentileRank()).isEqualByComparingTo(new BigDecimal("85"));
  }

  @Test
  @DisplayName("RS 데이터 없으면 예외 발생")
  void throwsExceptionWhenNoRsData() {
    assertThatThrownBy(() -> rsInsightService.query(stock.getCode(), LocalDate.now()))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("여러 RS 기록 중 최신 기록 반환")
  void returnsLatestRsWhenMultipleRecordsExist() {
    Kospi oldKospi = kospiRepository.save(new Kospi(2400L, LocalDate.now().minusDays(30)));
    Kospi newKospi = kospiRepository.save(new Kospi(2500L, LocalDate.now()));
    kospiRelativeStrengthRepository.saveAll(List.of(
        new KospiRelativeStrength(50, stock, oldKospi),
        new KospiRelativeStrength(75, stock, newKospi)
    ));

    RsResponse result = rsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.rsValue()).isEqualByComparingTo(new BigDecimal("75"));
  }
}
