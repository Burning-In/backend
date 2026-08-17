package com.momentum.domain.eps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.stock.Stock;
import com.momentum.sharedkernel.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.sharedkernel.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockEpsServiceTest {

  @Autowired
  private StockEpsService stockEpsService;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockEpsRepository stockEpsRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("직전 연도 같은 분기가 없으면 전년 대비 변동률은 비워 둔다")
  void create_withoutSameQuarterOfLastYear_leavesChangeRateNull() {
    // given
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "1200.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isNull();
  }

  @Test
  @DisplayName("직전 연도 같은 분기 대비 EPS 증감 비율을 전년 대비 변동률로 저장한다")
  void create_withSameQuarterOfLastYear_savesChangeRate() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(1_000.0, LocalDate.of(2023, 12, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "1200.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isEqualTo(0.2);
  }

  @Test
  @DisplayName("직전 연도라도 분기가 다르면 전년 대비 변동률을 계산하지 않는다")
  void create_withDifferentQuarterOfLastYear_leavesChangeRateNull() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(1_000.0, LocalDate.of(2023, 9, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "1200.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isNull();
  }

  @Test
  @DisplayName("EPS가 줄었으면 전년 대비 변동률이 음수가 된다")
  void create_whenEpsDecreased_savesNegativeChangeRate() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(1_000.0, LocalDate.of(2023, 12, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "800.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isEqualTo(-0.2);
  }

  @Test
  @DisplayName("적자가 줄었으면 개선이므로 변동률이 양수가 된다")
  void create_whenLossShrank_savesPositiveChangeRate() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(-100.0, LocalDate.of(2023, 12, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "-50.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isEqualTo(0.5);
  }

  @Test
  @DisplayName("적자에서 흑자로 돌아섰으면 변동률이 양수가 된다")
  void create_whenTurnedProfitable_savesPositiveChangeRate() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(-100.0, LocalDate.of(2023, 12, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "100.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isEqualTo(2.0);
  }

  @Test
  @DisplayName("적자가 커졌으면 악화이므로 변동률이 음수가 된다")
  void create_whenLossGrew_savesNegativeChangeRate() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(-100.0, LocalDate.of(2023, 12, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "-200.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isEqualTo(-1.0);
  }

  @Test
  @DisplayName("직전 연도 EPS가 0이었으면 변동률을 계산하지 않는다")
  void create_whenLastYearEpsWasZero_leavesChangeRateNull() {
    // given
    stockEpsRepository.saveAll(List.of(new StockEps(0.0, LocalDate.of(2023, 12, 1), null, stock)));
    List<StockEpsInfo> infos = List.of(StockEpsInfo.of(stock, "202412", "100.0"));

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertThat(result.getFirst().getYearOverYearChangeRate()).isNull();
  }

  @Test
  @DisplayName("여러 분기를 한 번에 저장한다")
  void create_withMultipleQuarters_savesAll() {
    // given
    List<StockEpsInfo> infos = List.of(
        StockEpsInfo.of(stock, "202409", "1100.0"),
        StockEpsInfo.of(stock, "202412", "1200.0")
    );

    // when
    List<StockEps> result = stockEpsService.create(infos);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result).hasSize(2);
      softly.assertThat(result).extracting(StockEps::getQuarter)
          .containsExactlyInAnyOrder(LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 1));
    });
  }
}
