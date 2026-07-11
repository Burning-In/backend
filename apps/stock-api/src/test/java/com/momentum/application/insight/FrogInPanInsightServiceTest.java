package com.momentum.application.insight;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.interfaces.api.stock.StockInsightV1Dto.FrogInPanResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    stock = stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("저장된 FIP 점수 반환")
  void returnsFipScoreFromRankScore() {
    saveRankScore(251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(stock.getCode(), TODAY);

    assertThat(result.fipScore()).isEqualByComparingTo(new BigDecimal("0.996032"));
  }

  @Test
  @DisplayName("252일 모두 상승이면 upDays = 251, downDays = 0")
  void countsUpDaysCorrectlyWhenAllDaysUp() {
    saveRankScore(251, 0);

    FrogInPanResponse result = frogInPanInsightService.query(stock.getCode(), TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(251);
    assertThat(result.yearlyDownDays()).isEqualTo(0);
  }

  @Test
  @DisplayName("252일 모두 하락이면 upDays = 0, downDays = 251")
  void countsDownDaysCorrectlyWhenAllDaysDown() {
    saveRankScore(0, 251);

    FrogInPanResponse result = frogInPanInsightService.query(stock.getCode(), TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(0);
    assertThat(result.yearlyDownDays()).isEqualTo(251);
  }

  @Test
  @DisplayName("상승일과 하락일이 절반씩이면 upDays = downDays")
  void upDaysEqualsDownDaysWhenEqualUpAndDown() {
    saveRankScore(125, 125);

    FrogInPanResponse result = frogInPanInsightService.query(stock.getCode(), TODAY);

    assertThat(result.yearlyUpDays()).isEqualTo(result.yearlyDownDays());
  }

  private void saveRankScore(int upDays, int downDays) {
    stockRankScoreRepository.save(StockRankScore.create(closePrices(upDays, downDays), TODAY, stock));
  }

  // 최신순 종가: 상승일 upDays개 + 하락일 downDays개
  private static List<Long> closePrices(int upDays, int downDays) {
    List<Long> prices = new ArrayList<>();
    long price = 1_000_000L;
    prices.add(price);
    for (int i = 0; i < upDays; i++) {
      prices.add(--price); // 최신(앞)이 더 큼 → 상승일
    }
    for (int i = 0; i < downDays; i++) {
      prices.add(++price); // 최신(앞)이 더 작음 → 하락일
    }
    return prices;
  }
}
