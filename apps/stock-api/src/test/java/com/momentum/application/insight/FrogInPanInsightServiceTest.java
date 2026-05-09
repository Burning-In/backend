package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.score.FipScore;
import com.momentum.domain.score.Momentum;
import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class FrogInPanInsightServiceTest {

  @Autowired FrogInPanInsightService frogInPanInsightService;
  @Autowired StockRepository stockRepository;
  @Autowired StockRankScoreRepository stockRankScoreRepository;

  private static final LocalDate TODAY = LocalDate.now();
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("저장된 FIP 점수 반환")
  void returnsFipScoreFromRankScore() {
    saveRankScore(new BigDecimal("0.9960"), 251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(stock, TODAY);

    assertThat(result.fipScore()).isEqualByComparingTo(new BigDecimal("0.9960"));
  }

  @Test
  @DisplayName("252일 모두 상승이면 upDays = 251, downDays = 0")
  void countsUpDaysCorrectlyWhenAllDaysUp() {
    saveRankScore(new BigDecimal("0.9960"), 251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(stock, TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(251);
    assertThat(result.yearlyDownDays()).isEqualTo(0);
  }

  @Test
  @DisplayName("252일 모두 하락이면 upDays = 0, downDays = 251")
  void countsDownDaysCorrectlyWhenAllDaysDown() {
    saveRankScore(new BigDecimal("-0.9960"), 0, 251);

    FrogInPanResponse result = frogInPanInsightService.query(stock, TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(0);
    assertThat(result.yearlyDownDays()).isEqualTo(251);
  }

  @Test
  @DisplayName("상승일과 하락일이 절반씩이면 upDays = downDays")
  void upDaysEqualsDownDaysWhenEqualUpAndDown() {
    saveRankScore(BigDecimal.ZERO, 125, 125);

    FrogInPanResponse result = frogInPanInsightService.query(stock, TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(result.yearlyDownDays());
  }

  private void saveRankScore(BigDecimal fip, int upDays, int downDays) {
    stockRankScoreRepository.save(StockRankScore.create(Momentum.of(BigDecimal.ONE), FipScore.of(fip, upDays, downDays), TODAY, stock));
  }
}
