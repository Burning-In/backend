package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.BREAKOUT_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.sharedkernel.StockRegime;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse.BreakoutReadyItem;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse.BreakoutSuccessItem;
import com.momentum.support.AnalysisTestData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RankingServiceTest {

  private static final LocalDate BASE_DATE = LocalDate.now();
  // 점수 baseDate(오늘) 이후 시점이어야 조회되고, 저장된 틱(createdAt=now)도 포함되도록 미래 시점을 사용한다.
  private static final LocalDateTime AT = LocalDateTime.now().plusDays(1);

  @Autowired
  private RankingService rankingService;
  @Autowired
  private AnalysisTestData analysisTestData;

  @Test
  @DisplayName("돌파 성공 랭킹은 BREAKOUT_SUCCESS 레짐 종목만 반환하고, 틱이 없으면 현재가는 null")
  void breakoutSuccessRankingReturnsOnlySuccessRegime() {
    saveScore("000020", BREAKOUT_SUCCESS, 0.5, 0.1);
    saveScore("000040", BREAKOUT_READY, 0.9, 0.1);

    BreakoutSuccessResponse response = rankingService.getBreakoutSuccessRanking(AT);

    assertSoftly(softly -> {
      softly.assertThat(response.stocks())
          .extracting(BreakoutSuccessItem::stockName)
          .containsExactly("종목000020");
      softly.assertThat(response.stocks().get(0).currentPrice()).isNull();
    });
  }

  @Test
  @DisplayName("돌파 성공 랭킹은 모멘텀 내림차순, 동점 시 FIP 오름차순으로 정렬된다")
  void breakoutSuccessRankingIsSortedByMomentumThenFip() {
    saveScore("000050", BREAKOUT_SUCCESS, 0.7, 0.2);
    saveScore("000070", BREAKOUT_SUCCESS, 0.7, 0.1);
    saveScore("000080", BREAKOUT_SUCCESS, 0.3, 0.9);

    BreakoutSuccessResponse response = rankingService.getBreakoutSuccessRanking(AT);

    assertThat(response.stocks())
        .extracting(BreakoutSuccessItem::stockName)
        .containsExactly("종목000070", "종목000050", "종목000080");
  }

  @Test
  @DisplayName("현재가는 조회 시점 기준 가장 최근 틱 체결가로 채워진다")
  void breakoutSuccessRankingFillsGetLastPriceFromTick() {
    saveScore("000100", BREAKOUT_SUCCESS, 0.42, 0.13);
    analysisTestData.saveTick("000100", 12_300L);
    analysisTestData.saveTick("000100", 12_500L); // 더 최근 틱

    BreakoutSuccessItem item = rankingService.getBreakoutSuccessRanking(AT).stocks().get(0);

    assertSoftly(softly -> {
      softly.assertThat(item.oneYearMomentum()).isEqualByComparingTo("0.42");
      softly.assertThat(item.fipScore()).isEqualByComparingTo("0.13");
      softly.assertThat(item.currentPrice()).isEqualByComparingTo("12500");
    });
  }

  @Test
  @DisplayName("다른 종목의 틱은 현재가로 섞이지 않는다")
  void breakoutSuccessRankingIgnoresOtherStockTick() {
    saveScore("000110", BREAKOUT_SUCCESS, 0.42, 0.13);
    analysisTestData.saveTick("000990", 99_900L);

    BreakoutSuccessItem item = rankingService.getBreakoutSuccessRanking(AT).stocks().get(0);

    assertThat(item.currentPrice()).isNull();
  }

  @Test
  @DisplayName("돌파준비 랭킹은 BREAKOUT_READY 레짐 종목만 반환한다")
  void breakoutReadyRankingReturnsOnlyReadyRegime() {
    saveScore("000120", BREAKOUT_READY, 0.5, 0.1);
    saveScore("000140", BREAKOUT_SUCCESS, 0.9, 0.1);

    BreakoutReadyResponse response = rankingService.getBreakoutReadyRanking(AT);

    assertThat(response.stocks())
        .extracting(BreakoutReadyItem::stockName)
        .containsExactly("종목000120");
  }

  @Test
  @DisplayName("돌파준비 랭킹 항목에 종목명/코드/현재가/모멘텀/FIP가 채워진다")
  void breakoutReadyRankingFillsItemFields() {
    saveScore("000121", BREAKOUT_READY, 0.42, 0.13);
    analysisTestData.saveTick("000121", 9_800L);
    analysisTestData.saveTick("000121", 9_900L); // 더 최근 틱

    BreakoutReadyItem item = rankingService.getBreakoutReadyRanking(AT).stocks().get(0);

    assertSoftly(softly -> {
      softly.assertThat(item.stockName()).isEqualTo("종목000121");
      softly.assertThat(item.stockCode()).isEqualTo("000121");
      softly.assertThat(item.currentPrice()).isEqualByComparingTo("9900");
      softly.assertThat(item.oneYearMomentum()).isEqualByComparingTo("0.42");
      softly.assertThat(item.fipScore()).isEqualByComparingTo("0.13");
    });
  }

  @Test
  @DisplayName("돌파준비 랭킹도 모멘텀 내림차순, 동점 시 FIP 오름차순으로 정렬된다")
  void breakoutReadyRankingIsSortedByMomentumThenFip() {
    saveScore("000220", BREAKOUT_READY, 0.7, 0.2);
    saveScore("000240", BREAKOUT_READY, 0.7, 0.1);
    saveScore("000270", BREAKOUT_READY, 0.3, 0.9);

    BreakoutReadyResponse response = rankingService.getBreakoutReadyRanking(AT);

    assertThat(response.stocks())
        .extracting(BreakoutReadyItem::stockName)
        .containsExactly("종목000240", "종목000220", "종목000270");
  }

  @Test
  @DisplayName("해당 레짐 종목이 없으면 빈 목록을 반환한다")
  void rankingReturnsEmptyWhenNoStocks() {
    BreakoutSuccessResponse response = rankingService.getBreakoutSuccessRanking(AT);

    assertThat(response.stocks()).isEmpty();
  }

  private void saveScore(String code, StockRegime regime, double momentum, double fip) {
    long stockId = analysisTestData.saveStock(code, regime);
    analysisTestData.saveRankScore(stockId, BASE_DATE, momentum, fip, 130, 120);
  }
}
