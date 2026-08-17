package com.momentum.infrastructure.ma;

import static com.momentum.sharedkernel.StockMovingAveragePeriod.MA_50;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.domain.movingaverage.StockMovingAverage;
import com.momentum.domain.movingaverage.StockMovingAverageRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.sharedkernel.StockMovingAveragePeriod;
import com.momentum.sharedkernel.StockRegime;
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
class StockMovingAverageRepositoryTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 5, 20);

  @Autowired
  private StockMovingAverageRepository stockMovingAverageRepository;
  @Autowired
  private StockRepository stockRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.UNKNOWN, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("여러 날이 쌓여 있으면 가장 최근 기준일의 이동평균만 조회된다")
  void findLatestByStock_returnsOnlyMostRecentBaseDate() {
    // given
    stockMovingAverageRepository.saveAll(List.of(
        new StockMovingAverage(1_111L, TODAY.minusDays(2), MA_50, stock),
        new StockMovingAverage(2_222L, TODAY.minusDays(1), MA_50, stock),
        new StockMovingAverage(3_333L, TODAY, MA_50, stock)
    ));

    // when
    List<StockMovingAverage> result = stockMovingAverageRepository.findLatestByStock(stock, TODAY);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result).hasSize(1);
      softly.assertThat(result.getFirst().getMa()).isEqualTo(3_333L);
    });
  }

  @Test
  @DisplayName("기준일 이후에 계산된 이동평균은 조회되지 않는다")
  void findLatestByStock_ignoresBaseDateAfterGivenDate() {
    // given
    stockMovingAverageRepository.saveAll(List.of(
        new StockMovingAverage(2_222L, TODAY.minusDays(1), MA_50, stock),
        new StockMovingAverage(3_333L, TODAY, MA_50, stock)
    ));

    // when
    List<StockMovingAverage> result = stockMovingAverageRepository.findLatestByStock(stock, TODAY.minusDays(1));

    // then
    assertSoftly(softly -> {
      softly.assertThat(result).hasSize(1);
      softly.assertThat(result.getFirst().getMa()).isEqualTo(2_222L);
    });
  }

  @Test
  @DisplayName("다른 종목의 이동평균은 조회되지 않는다")
  void findLatestByStock_ignoresOtherStock() {
    // given
    Stock otherStock = stockRepository.save(Stock.of("하이닉스", "000660", StockRegime.UNKNOWN, StockTrend.UPTREND));
    stockMovingAverageRepository.saveAll(List.of(
        new StockMovingAverage(1_111L, TODAY, MA_50, stock),
        new StockMovingAverage(9_999L, TODAY, MA_50, otherStock)
    ));

    // when
    List<StockMovingAverage> result = stockMovingAverageRepository.findLatestByStock(stock, TODAY);

    // then
    assertSoftly(softly -> {
      softly.assertThat(result).hasSize(1);
      softly.assertThat(result.getFirst().getMa()).isEqualTo(1_111L);
    });
  }
}
