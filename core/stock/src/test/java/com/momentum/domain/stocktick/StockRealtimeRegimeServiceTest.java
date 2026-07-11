package com.momentum.domain.stocktick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.pricepoint.StockPricePointRepository;
import com.momentum.domain.pricepoint.entity.StockPricePoint;
import com.momentum.domain.pricepoint.entity.StockPricePointType;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockRealtimeRegimeServiceTest {

  @Autowired
  private StockRealtimeRegimeService stockRealtimeRegimeService;
  @Autowired
  private StockRepository stockRepository;
  @Autowired
  private StockBaseRepository stockBaseRepository;
  @Autowired
  private StockPricePointRepository stockPricePointRepository;

  @Test
  @DisplayName("VCP이고 현재가가 저항선 상단을 돌파하면 BREAKOUT_SUCCESS로 갱신된다")
  void updatesToBreakoutSuccess() {
    Stock stock = saveStock("000040", StockRegime.BREAKOUT_READY);
    saveBase(stock, 10_000L, 8_000L, true);
    saveExtraPoints(stock);

    // 저항선 상단 = 10_000 * 1.05 = 10_500, 현재가 11_000 > 10_500
    stockRealtimeRegimeService.resolveRealtimeRegime("000040", 11_000L);

    StockRegime saved = stockRepository.findByStockCode("000040").orElseThrow().getStockRegime();
    assertThat(saved).isEqualTo(StockRegime.BREAKOUT_SUCCESS);
  }

  @Test
  @DisplayName("VCP가 아니면 UNKNOWN이라 기존 레짐을 유지한다")
  void keepsRegimeWhenUnknown() {
    Stock stock = saveStock("000050", StockRegime.BREAKOUT_READY);
    saveBase(stock, 10_000L, 8_000L, false);
    saveExtraPoints(stock);

    stockRealtimeRegimeService.resolveRealtimeRegime("000050", 10_000L);

    StockRegime saved = stockRepository.findByStockCode("000050").orElseThrow().getStockRegime();
    assertThat(saved).isEqualTo(StockRegime.BREAKOUT_READY);
  }

  @Test
  @DisplayName("종목을 찾지 못하면 IllegalArgumentException")
  void throwsWhenStockNotFound() {
    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime("000270", 10_000L))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("현재 베이스가 없으면 IllegalStateException")
  void throwsWhenBaseNotFound() {
    Stock stock = saveStock("000070", StockRegime.BREAKOUT_READY);
    saveExtraPoints(stock);

    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime("000070", 10_000L))
        .isInstanceOf(IllegalStateException.class);
  }

  private Stock saveStock(String code, StockRegime regime) {
    return stockRepository.save(Stock.of("테스트종목", code, regime, StockTrend.UPTREND));
  }

  private void saveBase(Stock stock, long resistancePrice, long supportPrice, boolean vcp) {
    StockPricePoint high = new StockPricePoint(resistancePrice, 100_000L, LocalDate.now().minusDays(10),
        StockPricePointType.HIGH, null, stock);
    StockPricePoint low = new StockPricePoint(supportPrice, 100_000L, LocalDate.now().minusDays(20),
        StockPricePointType.LOW, null, stock);
    StockBase base = StockBase.init(high, low, 100_000L);
    if (vcp) {
      // 변동성이 줄어드는(직전 > 직후) 이력 → isVcp = true
      base.update(null, List.of(100L, 50L));
    }
    stockBaseRepository.save(base);
  }

  // resolveType()이 최근 가격포인트 3개 이상을 요구하므로 충분한 포인트를 적재한다.
  private void saveExtraPoints(Stock stock) {
    stockPricePointRepository.save(new StockPricePoint(9_500L, 100_000L, LocalDate.now().minusDays(3),
        StockPricePointType.HIGH, null, stock));
    stockPricePointRepository.save(new StockPricePoint(9_000L, 100_000L, LocalDate.now().minusDays(2),
        StockPricePointType.LOW, null, stock));
    stockPricePointRepository.save(new StockPricePoint(9_300L, 100_000L, LocalDate.now().minusDays(1),
        StockPricePointType.HIGH, null, stock));
  }
}
