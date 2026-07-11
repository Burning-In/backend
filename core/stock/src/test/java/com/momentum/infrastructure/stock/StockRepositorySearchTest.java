package com.momentum.infrastructure.stock;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stock.StockTrend;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockRepositorySearchTest {

  @Autowired
  private StockRepository stockRepository;

  @BeforeEach
  void setUp() {
    stockRepository.save(Stock.of("삼성전자", "005930", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
    stockRepository.save(Stock.of("하이닉스", "000660", StockRegime.DIRECTION_UNDETERMINED, StockTrend.UPTREND));
  }

  @Test
  @DisplayName("코드 부분일치로 검색된다 — StockCode가 코드값으로 저장되어 stringValue().like가 동작한다")
  void searchByCodeSubstring() {
    // 이름엔 '5930'이 없고 코드 '005930'에만 있으므로, 코드 검색 경로만 검증된다
    List<Stock> result = stockRepository.search("5930");

    assertThat(result).extracting(Stock::getName).containsExactly("삼성전자");
  }

  @Test
  @DisplayName("다른 종목의 코드 부분일치도 정확히 그 종목만 반환한다")
  void searchByOtherCodeSubstring() {
    List<Stock> result = stockRepository.search("0066");

    assertThat(result).extracting(Stock::getName).containsExactly("하이닉스");
  }

  @Test
  @DisplayName("이름 부분일치로 검색된다")
  void searchByNameSubstring() {
    List<Stock> result = stockRepository.search("하이");

    assertThat(result).extracting(Stock::getName).containsExactly("하이닉스");
  }

  @Test
  @DisplayName("코드·이름 어디에도 없으면 빈 결과")
  void searchNoMatch() {
    List<Stock> result = stockRepository.search("존재하지않는쿼리");

    assertThat(result).isEmpty();
  }
}
