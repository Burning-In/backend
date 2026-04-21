package com.momentum.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockDailyCandle;
import com.momentum.domain.entity.StockRegime;
import com.momentum.domain.entity.StockTrend;
import com.momentum.domain.entity.indicator.price.StockPricePoint;
import com.momentum.domain.entity.indicator.price.StockPivotCalculateHistory;
import com.momentum.domain.respository.StockCandleRepository;
import com.momentum.domain.respository.StockPivotCalculateHistoryRepository;
import com.momentum.domain.respository.StockPricePointRepository;
import com.momentum.domain.respository.StockRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockPricePointServiceTest {

  @Autowired
  private StockPricePointService stockPricePointService;
  @Autowired
  private StockCandleRepository stockCandleRepository;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockPricePointRepository stockPricePointRepository;
  @Autowired
  private StockPivotCalculateHistoryRepository stockPivotCalculateHistoryRepository;

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.UNDETERMINED, StockTrend.OTHER));
  }

  @Test
  @DisplayName("히스토리 없음 + 피벗 없음 → 첫 포인트, 피벗만 저장")
  void resolvePivot_firstPoint_noPivotNoHistory() {
    // given
    StockDailyCandle candle = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240101", 10000L, 11000L, 9500L, 10500L, 1000L, "2")
    );

    // when
    stockPricePointService.resolvePricePoint(candle);

    // then
    Optional<StockPricePoint> savedPivot = stockPricePointRepository.findTopByStockOrderByCreatedAtDesc(stock);
    assertThat(savedPivot).isPresent();
    assertThat(savedPivot.get().getPrice()).isEqualTo(10500L);

    Optional<StockPivotCalculateHistory> history = stockPivotCalculateHistoryRepository.findTopCalculationHistory(stock);
    assertThat(history).isEmpty();
  }

  @Test
  @DisplayName("히스토리 없음 + 피벗 있음 → SU, SL 계산 후 히스토리 저장")
  void resolvePivot_noPivotHistory_withPivot() {
    // given
    // 피벗 먼저 저장 (A 포인트 역할)
    StockDailyCandle pivotCandle = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240101", 10000L, 11000L, 9500L, 10000L, 1000L, "2")
    );
    stockPricePointService.resolvePricePoint(pivotCandle); // 피벗만 저장됨

    // 다음 포인트 (B 포인트 역할)
    StockDailyCandle nextCandle = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240102", 10200L, 10800L, 9800L, 10500L, 1200L, "2")
    );

    // when
    stockPricePointService.resolvePricePoint(nextCandle);

    // then
    Optional<StockPivotCalculateHistory> history = stockPivotCalculateHistoryRepository.findTopCalculationHistory(stock);
    assertThat(history).isPresent();
    assertThat(history.get().getSU_MAX()).isNotNull();
    assertThat(history.get().getSL_MIN()).isNotNull();
    assertThat(history.get().getSU_MAX().compareTo(history.get().getSL_MIN())).isLessThan(0);
  }

  @Test
  @DisplayName("히스토리 있음 + 정상 갱신 → suMax > slMin, 히스토리 업데이트")
  void resolvePivot_withHistory_Update_SU_BIGGER_SL() {
    // given
    // A 포인트
    StockDailyCandle candleA = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240101", 10000L, 11000L, 9500L, 10000L, 1000L, "2")
    );
    stockPricePointService.resolvePricePoint(candleA);

    // B 포인트
    StockDailyCandle candleB = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240102", 10200L, 10800L, 9800L, 10500L, 1200L, "2")
    );
    stockPricePointService.resolvePricePoint(candleB);

    // C 포인트 (도어 안에 들어오는 포인트)
    StockDailyCandle candleC = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240103", 10300L, 10900L, 9900L, 10600L, 1100L, "2")
    );

    // when
    stockPricePointService.resolvePricePoint(candleC);

    // then
    Optional<StockPivotCalculateHistory> history = stockPivotCalculateHistoryRepository.findTopCalculationHistory(stock);
    assertThat(history).isPresent();
    assertThat(history.get().getCurrentPrice()).isEqualTo(10600L);
    assertThat(history.get().getSU_MAX().compareTo(history.get().getSL_MIN())).isLessThan(0);
  }

  @Test
  @DisplayName("히스토리 있음 + 정상 갱신 → suMax <= slMin, 히스토리 업데이트")
  void resolvePivot_withHistory_normalUpdate() {
    // given
    // A 포인트
    StockDailyCandle candleA = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240101", 10000L, 11000L, 9500L, 10000L, 1000L, "2")
    );
    stockPricePointService.resolvePricePoint(candleA);

    // B 포인트
    StockDailyCandle candleB = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240102", 10200L, 10800L, 9800L, 10200L, 1200L, "2")
    );
    stockPricePointService.resolvePricePoint(candleB);

    // C 포인트 (도어 안에 들어오는 포인트)
    StockDailyCandle candleC = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240103", 10300L, 10900L, 9900L, 10500L, 1100L, "2")
    );

    // when
    stockPricePointService.resolvePricePoint(candleC);

    // then
    Optional<StockPivotCalculateHistory> history = stockPivotCalculateHistoryRepository.findTopCalculationHistory(stock);
    assertThat(history).isPresent();
    assertThat(history.get().getCurrentPrice()).isEqualTo(10500L);
    // 정상 갱신이므로 SU_MAX < SL_MIN 유지
    assertThat(history.get().getSU_MAX().compareTo(history.get().getSL_MIN())).isLessThan(0);
  }

  @Test
  @DisplayName("히스토리 있음 + 역전 → 새 피벗 생성, 히스토리 재초기화")
  void resolvePivot_withHistory_pivotReset() {
    // given
    // A 포인트 (피벗, 가격 10000)
    StockDailyCandle candleA = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240101", 10000L, 11000L, 9500L, 10000L, 1000L, "2")
    );
    stockPricePointService.resolvePricePoint(candleA);

    // B 포인트
    StockDailyCandle candleB = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240102", 10200L, 10800L, 9800L, 10300L, 1200L, "2")
    );
    stockPricePointService.resolvePricePoint(candleB);

    // G 포인트 (마지막 범위내 포인트 역할, 내일이 H가 됨)
    StockDailyCandle candleG = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240103", 10400L, 11000L, 10000L, 10500L, 1300L, "2")
    );
    stockPricePointService.resolvePricePoint(candleG);

    // H 포인트 (역전 유발, 급등)
    StockDailyCandle candleH = stockCandleRepository.save(
        StockDailyCandle.create(stock, "20240104", 13000L, 15000L, 12000L, 14000L, 5000L, "2")
    );

    // when
    stockPricePointService.resolvePricePoint(candleH);

    // then
    // 새 피벗이 생성됨 (G = 어제 = 20240103)
    Optional<StockPricePoint> newPivot = stockPricePointRepository.findTopByStockOrderByCreatedAtDesc(stock);
    assertThat(newPivot).isPresent();
    assertThat(newPivot.get().getPrice()).isEqualTo(10500L); // G의 closePrice

    // 히스토리가 새 피벗 기준으로 재초기화됨
    Optional<StockPivotCalculateHistory> history = stockPivotCalculateHistoryRepository.findTopCalculationHistory(stock);
    assertThat(history).isPresent();
    assertThat(history.get().getStockPricePoint().getPrice()).isEqualTo(10500L);
    assertThat(history.get().getSU_MAX().compareTo(history.get().getSL_MIN())).isLessThan(0);
  }
}
