package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.eps.StockEps;
import com.momentum.domain.eps.StockEpsRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.EpsResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class EpsInsightServiceTest {

  @Autowired EpsInsightService epsInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired StockEpsRepository stockEpsRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("EPS 데이터 없으면 빈 리스트 반환")
  void returnsEmptyListWhenNoEpsData() {
    EpsResponse result = epsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.quarterlyEps()).isEmpty();
    assertThat(result.changeRateYoY()).isNull();
  }

  @Test
  @DisplayName("6분기 있어도 최근 5분기만 반환")
  void returnsOnlyFiveQuartersWhenMoreExist() {
    saveQuarterlyEps(6);

    EpsResponse result = epsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.quarterlyEps()).hasSize(5);
  }

  @Test
  @DisplayName("3분기만 있으면 3분기 반환")
  void returnsThreeQuartersWhenOnlyThreeExist() {
    saveQuarterlyEps(3);

    EpsResponse result = epsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.quarterlyEps()).hasSize(3);
  }

  @Test
  @DisplayName("최신 분기의 YoY 변동률 반환")
  void returnsYoyChangeRateFromLatestQuarter() {
    YearMonth latestQuarter = YearMonth.now();
    stockEpsRepository.saveAll(List.of(
        new StockEps(1500.0, latestQuarter, 0.25, stock),
        new StockEps(1200.0, latestQuarter.minusMonths(3), null, stock)
    ));

    EpsResponse result = epsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.changeRateYoY()).isEqualByComparingTo("0.2500");
  }

  @Test
  @DisplayName("분기명이 YearMonth 형식으로 반환")
  void returnsQuarterNameInYearMonthFormat() {
    stockEpsRepository.saveAll(List.of(new StockEps(1000.0, YearMonth.of(2024, 3), null, stock)));

    EpsResponse result = epsInsightService.query(stock.getCode(), LocalDate.now());

    assertThat(result.quarterlyEps().get(0).quarter()).isEqualTo("2024-03");
  }

  private void saveQuarterlyEps(int count) {
    YearMonth base = YearMonth.now();
    List<StockEps> epsList = IntStream.rangeClosed(0, count - 1)
        .mapToObj(i -> new StockEps(1000.0 + i * 100, base.minusMonths((long) i * 3), null, stock))
        .toList();
    stockEpsRepository.saveAll(epsList);
  }
}
