package com.momentum.domain.eps;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.time.YearMonth;
import java.util.List;
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

  @Test
  void YoY_데이터가_없으면_null로_저장된다() {
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    List<StockEpsInfo> infos = List.of(
        StockEpsInfo.of(stock, "202412", "1200.0")
    );

    List<StockEps> result = stockEpsService.create(infos);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getYearOverYear()).isNull();
  }

  @Test
  void YoY_데이터가_있으면_계산해서_저장된다() {
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    stockEpsRepository.saveAll(List.of(
        new StockEps(1000.0, YearMonth.of(2023, 12), null, stock)
    ));
    List<StockEpsInfo> infos = List.of(
        StockEpsInfo.of(stock, "202412", "1200.0")
    );

    List<StockEps> result = stockEpsService.create(infos);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getYearOverYear()).isEqualTo(0.2);
  }

  @Test
  void 여러_분기_EPS를_한번에_저장한다() {
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    List<StockEpsInfo> infos = List.of(
        StockEpsInfo.of(stock, "202409", "1100.0"),
        StockEpsInfo.of(stock, "202412", "1200.0")
    );

    List<StockEps> result = stockEpsService.create(infos);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(StockEps::getQuarterlyDate)
        .containsExactlyInAnyOrder(YearMonth.of(2024, 9), YearMonth.of(2024, 12));
  }
}
