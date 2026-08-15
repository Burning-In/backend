package com.momentum.domain.stocktick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.momentum.domain.base.StockBaseRepository;
import com.momentum.domain.base.entity.StockBase;
import com.momentum.domain.anchorpoint.StockAnchorPointRepository;
import com.momentum.domain.anchorpoint.entity.StockAnchorPoint;
import com.momentum.domain.anchorpoint.entity.StockAnchorPointType;
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
  private StockAnchorPointRepository stockAnchorPointRepository;

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

  @Test
  @DisplayName("베이스는 있지만 유효한 가격 특이점이 없으면 IllegalStateException")
  void throwsWhenLastAnchorPointNotFound() {
    Stock stock = saveStock("000090", StockRegime.BREAKOUT_READY);
    // 베이스에 딸린 고점/저점 특이점을 모두 소프트 삭제해, 조회 가능한 특이점이 하나도 없는 상황을 만든다.
    StockAnchorPoint high = new StockAnchorPoint(10_000L, 100_000L, LocalDate.now().minusDays(10),
        StockAnchorPointType.HIGH, null, stock);
    StockAnchorPoint low = new StockAnchorPoint(8_000L, 100_000L, LocalDate.now().minusDays(20),
        StockAnchorPointType.LOW, null, stock);
    StockBase base = StockBase.init(high, low, 100_000L);
    base.update(null, List.of(100L, 50L));
    stockBaseRepository.save(base);
    high.delete();
    low.delete();
    stockAnchorPointRepository.save(high);
    stockAnchorPointRepository.save(low);

    assertThatThrownBy(() -> stockRealtimeRegimeService.resolveRealtimeRegime("000090", 10_000L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("실시간에서 VCP이고 현재가가 저항선 상단을 돌파하면 BREAKOUT_SUCCESS로 갱신된다")
  void updatesToBreakoutSuccess() {
    Stock stock = saveStock("000040", StockRegime.BREAKOUT_READY);
    saveBase(stock, 10_000L, 8_000L);
    saveExtraPoints(stock);

    // 저항선 상단 = 10_000 * 1.05 = 10_500, 현재가 11_000 > 10_500
    stockRealtimeRegimeService.resolveRealtimeRegime("000040", 11_000L);

    StockRegime saved = stockRepository.findByStockCode("000040").orElseThrow().getStockRegime();
    assertThat(saved).isEqualTo(StockRegime.BREAKOUT_SUCCESS);
  }

  private Stock saveStock(String code, StockRegime regime) {
    return stockRepository.save(Stock.of("테스트종목", code, regime, StockTrend.UPTREND));
  }

  private void saveBase(Stock stock, long resistancePrice, long supportPrice) {
    StockAnchorPoint high = new StockAnchorPoint(resistancePrice, 100_000L, LocalDate.now().minusDays(10),
        StockAnchorPointType.HIGH, null, stock);
    StockAnchorPoint low = new StockAnchorPoint(supportPrice, 100_000L, LocalDate.now().minusDays(20),
        StockAnchorPointType.LOW, null, stock);
    StockBase base = StockBase.init(high, low, 100_000L);
    // 변동성이 줄어드는(직전 > 직후) 이력 → isVcp = true
    base.update(null, List.of(100L, 50L));
    stockBaseRepository.save(base);
  }

  // resolvePointTypes()이 최근 가격포인트 3개 이상을 요구하므로 충분한 포인트를 적재한다.
  private void saveExtraPoints(Stock stock) {
    stockAnchorPointRepository.save(new StockAnchorPoint(9_500L, 100_000L, LocalDate.now().minusDays(3),
        StockAnchorPointType.HIGH, null, stock));
    stockAnchorPointRepository.save(new StockAnchorPoint(9_000L, 100_000L, LocalDate.now().minusDays(2),
        StockAnchorPointType.LOW, null, stock));
    stockAnchorPointRepository.save(new StockAnchorPoint(9_300L, 100_000L, LocalDate.now().minusDays(1),
        StockAnchorPointType.HIGH, null, stock));
  }
}
