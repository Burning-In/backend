package com.momentum.application;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.score.StockRankScore;
import com.momentum.domain.score.StockRankScoreRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.TrackedStock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import com.momentum.domain.stocktick.StockTick;
import com.momentum.domain.stocktick.StockTickRepository;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutReadyResponse.BreakoutReadyItem;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse;
import com.momentum.interfaces.api.rank.RankingV1Dto.BreakoutSuccessResponse.BreakoutSuccessItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
  private StockRankScoreRepository stockRankScoreRepository;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockTickRepository stockTickRepository;

  @Test
  @DisplayName("돌파 성공 랭킹은 BREAKOUT_SUCCESS 레짐 종목만 반환하고, 틱이 없으면 현재가는 null")
  void breakoutSuccessRankingReturnsOnlySuccessRegime() {
    saveScore(saveStock("000020", BREAKOUT_SUCCESS), 0.5, 0.1);
    saveScore(saveStock("000040", BREAKOUT_READY), 0.9, 0.1);

    BreakoutSuccessResponse response = rankingService.getBreakoutSuccessRanking(AT);

    assertThat(response.stocks())
        .extracting(BreakoutSuccessItem::stockName)
        .containsExactly("종목000020");
    assertThat(response.stocks().get(0).currentPrice()).isNull();
  }

  @Test
  @DisplayName("돌파 성공 랭킹은 모멘텀 내림차순, 동점 시 FIP 오름차순으로 정렬된다")
  void breakoutSuccessRankingIsSortedByMomentumThenFip() {
    saveScore(saveStock("000050", BREAKOUT_SUCCESS), 0.7, 0.2);
    saveScore(saveStock("000070", BREAKOUT_SUCCESS), 0.7, 0.1);
    saveScore(saveStock("000080", BREAKOUT_SUCCESS), 0.3, 0.9);

    BreakoutSuccessResponse response = rankingService.getBreakoutSuccessRanking(AT);

    assertThat(response.stocks())
        .extracting(BreakoutSuccessItem::stockName)
        .containsExactly("종목000070", "종목000050", "종목000080");
  }

  @Test
  @DisplayName("현재가는 조회 시점 기준 가장 최근 틱 체결가로 채워진다")
  void breakoutSuccessRankingFillsGetLastPriceFromTick() {
    Stock stock = saveStock("000100", BREAKOUT_SUCCESS);
    saveScore(stock, 0.42, 0.13);
    saveTick("000100", 12_300L);
    saveTick("000100", 12_500L); // 더 최근 틱

    BreakoutSuccessItem item = rankingService.getBreakoutSuccessRanking(AT).stocks().get(0);

    assertThat(item.oneYearMomentum()).isEqualByComparingTo("0.42");
    assertThat(item.fipScore()).isEqualByComparingTo("0.130952"); // 33/252 (0.13에 가장 근접한 계산값)
    assertThat(item.currentPrice()).isEqualByComparingTo("12500");
  }

  @Test
  @DisplayName("돌파준비 랭킹은 BREAKOUT_READY 레짐 종목만 반환한다")
  void breakoutReadyRankingReturnsOnlyReadyRegime() {
    saveScore(saveStock("000120", BREAKOUT_READY), 0.5, 0.1);
    saveScore(saveStock("000140", BREAKOUT_SUCCESS), 0.9, 0.1);

    BreakoutReadyResponse response = rankingService.getBreakoutReadyRanking(AT);

    assertThat(response.stocks())
        .extracting(BreakoutReadyItem::stockName)
        .containsExactly("종목000120");
  }

  @Test
  @DisplayName("돌파준비 랭킹 항목에 종목명/코드/현재가/모멘텀/FIP가 채워진다")
  void breakoutReadyRankingFillsItemFields() {
    Stock stock = saveStock("000120", BREAKOUT_READY);
    saveScore(stock, 0.42, 0.13);
    saveTick("000120", 9_800L);
    saveTick("000120", 9_900L); // 더 최근 틱

    BreakoutReadyItem item = rankingService.getBreakoutReadyRanking(AT).stocks().get(0);

    assertThat(item.stockName()).isEqualTo("종목000120");
    assertThat(item.stockCode()).isEqualTo("000120");
    assertThat(item.currentPrice()).isEqualByComparingTo("9900");
    assertThat(item.oneYearMomentum()).isEqualByComparingTo("0.42");
    assertThat(item.fipScore()).isEqualByComparingTo("0.130952");
  }

  @Test
  @DisplayName("돌파준비 랭킹도 모멘텀 내림차순, 동점 시 FIP 오름차순으로 정렬된다")
  void breakoutReadyRankingIsSortedByMomentumThenFip() {
    saveScore(saveStock("000220", BREAKOUT_READY), 0.7, 0.2);
    saveScore(saveStock("000240", BREAKOUT_READY), 0.7, 0.1);
    saveScore(saveStock("000270", BREAKOUT_READY), 0.3, 0.9);

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

  private Stock saveStock(String code, StockRegime regime) {
    return stockRepository.save(Stock.of("종목" + code, code, regime, StockTrend.UPTREND));
  }

  private void saveScore(Stock stock, double momentum, double fip) {
    long yearAgo = 100_000L;
    long today = Math.round(yearAgo * (1 + momentum));   // momentum = (today - yearAgo) / yearAgo
    int upDays = Math.max(1, (int) Math.round(fip * 252)); // fip = upDays / 252 (하락일 0)
    stockRankScoreRepository.save(StockRankScore.create(upPath(today, yearAgo, upDays), BASE_DATE, stock));
  }

  // 최신순 종가: today에서 yearAgo까지 내려오는 upDays개 상승일 경로
  private static List<Long> upPath(long today, long yearAgo, int upDays) {
    List<Long> prices = new ArrayList<>();
    long span = today - yearAgo;
    for (int i = 0; i < upDays; i++) {
      prices.add(today - span * i / upDays);
    }
    prices.add(yearAgo);
    return prices;
  }

  private void saveTick(String code, long price) {
    stockTickRepository.save(
        StockTick.create(LocalDate.of(2024, 1, 1), "090000", price, 1L, 1L, TrackedStock.fromCode(code)));
  }
}
