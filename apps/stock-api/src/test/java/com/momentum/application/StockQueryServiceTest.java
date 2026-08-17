package com.momentum.application;

import static com.momentum.sharedkernel.StockRegime.BREAKOUT_READY;
import static com.momentum.sharedkernel.StockRegime.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.momentum.infrastructure.query.StockMetaRow;
import com.momentum.sharedkernel.StockTrend;
import com.momentum.support.AnalysisTestData;
import com.momentum.support.error.CoreException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class StockQueryServiceTest {

  @Autowired
  private StockQueryService stockQueryService;
  @Autowired
  private AnalysisTestData analysisTestData;

  @BeforeEach
  void setUp() {
    analysisTestData.saveStock("005930", "삼성전자", UNKNOWN, StockTrend.UPTREND);
    analysisTestData.saveStock("000660", "하이닉스", UNKNOWN, StockTrend.UPTREND);
  }

  @Test
  @DisplayName("코드 부분일치로 검색된다")
  void searchByCodeSubstring() {
    List<StockMetaRow> result = stockQueryService.search("5930");

    assertThat(result).extracting(StockMetaRow::stockName).containsExactly("삼성전자");
  }

  @Test
  @DisplayName("이름 부분일치로 검색된다")
  void searchByNameSubstring() {
    List<StockMetaRow> result = stockQueryService.search("하이");

    assertThat(result).extracting(StockMetaRow::stockName).containsExactly("하이닉스");
  }

  @Test
  @DisplayName("코드·이름 어디에도 없으면 빈 결과")
  void searchNoMatch() {
    List<StockMetaRow> result = stockQueryService.search("존재하지않는쿼리");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("이름 접두 일치가 부분 일치보다 앞에, 코드 일치가 그 뒤에 온다")
  void searchOrdersByMatchAccuracy() {
    analysisTestData.saveStock("000100", "삼성물산", UNKNOWN, StockTrend.UPTREND);
    analysisTestData.saveStock("000200", "미래삼성", UNKNOWN, StockTrend.UPTREND);
    analysisTestData.saveStock("003300", "코드만일치", UNKNOWN, StockTrend.UPTREND);

    List<StockMetaRow> result = stockQueryService.search("삼성");

    // 접두 일치(삼성전자·삼성물산) → 부분 일치(미래삼성). 접두 일치끼리는 등록 순서
    assertThat(result).extracting(StockMetaRow::stockName)
        .containsExactly("삼성전자", "삼성물산", "미래삼성");
  }

  @Test
  @DisplayName("코드로 단건 조회하면 종목명·레짐·추세를 반환한다")
  void getByCodeReturnsMeta() {
    analysisTestData.saveStock("000300", BREAKOUT_READY);

    StockMetaRow result = stockQueryService.getByCode("000300");

    assertSoftly(softly -> {
      softly.assertThat(result.stockCode()).isEqualTo("000300");
      softly.assertThat(result.stockName()).isEqualTo("종목000300");
      softly.assertThat(result.stockRegime()).isEqualTo(BREAKOUT_READY.name());
    });
  }

  @Test
  @DisplayName("없는 코드로 단건 조회하면 예외")
  void getByCodeThrowsWhenNotFound() {
    assertThatThrownBy(() -> stockQueryService.getByCode("999999"))
        .isInstanceOf(CoreException.class);
  }
}
