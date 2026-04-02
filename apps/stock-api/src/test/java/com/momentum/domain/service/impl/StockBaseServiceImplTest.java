package com.momentum.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.indicator.StockBase;
import com.momentum.domain.entity.indicator.StockBaseLine;
import com.momentum.domain.entity.indicator.StockBaseType;
import com.momentum.domain.entity.indicator.StockLine;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockLineRepository;
import com.momentum.domain.respository.StockRepository;
import com.momentum.domain.service.StockBaseService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class StockBaseServiceImplTest {

  @Autowired
  private StockBaseService stockBaseService;

  @Autowired
  private StockRepository stockRepository;

  @Autowired
  private StockLineRepository stockLineRepository;

  @Autowired
  private StockBaseRepository stockBaseRepository;

  @Test
  @DisplayName("이전 베이스가 없으면 accumulationCount = 1으로 생성된다")
  void createCandidate_noPreviousBase() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930"));
    StockLine triggerLine = stockLineRepository.save(
        StockLine.resistance(100_000L, stock)
    );

    // when
    StockBase result = stockBaseService.createCandidate(stock, triggerLine);

    // then
    assertThat(result.getAccumulationCount()).isEqualTo(1L);
    assertThat(result.getStock()).isEqualTo(stock);
  }

  @Test
  @DisplayName("저항선 돌파 이후, 후보 베이스 첫번쨰 SUPPORT가 이전 저항보다 낮으면 병합된다")
  void evaluateBase_support_merge() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930"));

    StockLine resistance = stockLineRepository.save(
        StockLine.resistance(100_000L, stock)
    );

    // 이전 confirmed base 생성
    StockBase previousBase = stockBaseService.createCandidate(stock, resistance);
    StockLine previousSupport = stockLineRepository.save(
        StockLine.support(80_000L, stock));

    previousBase.confirm(previousSupport);
    stockBaseRepository.save(previousBase);

    // candidate 생성
    StockLine newResistance = stockLineRepository.save(
        StockLine.resistance(110_000L, stock)
    );
    StockBase candidate = stockBaseService.createCandidate(stock, newResistance);

    // SUPPORT가 이전 저항보다 낮음 → merge 조건 : 이게 무엇인가?
    StockLine support = stockLineRepository.save(
        StockLine.support(90_000L, stock)
    );

    // when
    stockBaseService.evaluateBase(stock, support);

    // then
    assertThat(stockBaseRepository.findLastBase(stock.getId(), StockBaseType.CANDIDATE)).isEmpty();
    assertThat(previousBase)
        .extracting(StockBase::getHighestResistancePrice, StockBase::getLowestSupportLinePrice, StockBase::getAccumulationCount)
        .containsExactlyInAnyOrder(110_000L, 80_000L, 1L);
    assertThat(previousBase.getStockBaseLines())
        .extracting(StockBaseLine::getStockLine)
        .extracting(StockLine::getPrice)
        .containsExactlyInAnyOrder(100_000L, 110_000L, 80_000L, 90_000L);
  }

  @Test
  @DisplayName("저항선 돌파 이후 후보베이스 SUPPORT가 정상 범위면 confirm 된다")
  void evaluateBase_support_confirm() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930"));

    StockLine resistance = stockLineRepository.save(
        StockLine.resistance(100_000L, stock)
    );

    StockBase previous = stockBaseService.createCandidate(stock, resistance);
    previous.confirm(resistance);
    stockBaseRepository.save(previous);

    StockLine newResistance = stockLineRepository.save(
        StockLine.resistance(110_000L, stock)
    );
    StockBase candidate = stockBaseService.createCandidate(stock, newResistance);

    // 정상 범위 (이전 저항보다 위)
    StockLine support = stockLineRepository.save(
        StockLine.support(105_000L, stock)
    );

    // when
    stockBaseService.evaluateBase(stock, support);

    // then
    StockBase updated = stockBaseRepository
        .findLastBase(stock.getId(), StockBaseType.CONFIRMED)
        .orElseThrow();

    assertThat(updated.getId()).isEqualTo(candidate.getId());
  }

  // 이건 뭐가 문제지
  @Test
  @DisplayName("지지선 하락돌파이후 후보베이스 RESISTANCE가 이전 지지보다 낮으면 병합된다")
  void evaluateBase_resistance_merge() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930"));

    StockLine supportLine = stockLineRepository.save(
        StockLine.support(90_000L, stock)
    );

    StockBase previous = stockBaseService.createCandidate(stock, supportLine);
    previous.confirm(supportLine);
    stockBaseRepository.save(previous);

    StockLine trigger = stockLineRepository.save(
        StockLine.resistance(100_000L, stock)
    );
    StockBase candidate = stockBaseService.createCandidate(stock, trigger);

    // 이전 지지보다 낮음 → merge
    StockLine resistance = stockLineRepository.save(
        StockLine.resistance(80_000L, stock)
    );

    // when
    stockBaseService.evaluateBase(stock, resistance);

    // then
    assertThat(stockBaseRepository.findLastBase(stock.getId(), StockBaseType.CANDIDATE)).isEmpty();
  }

  @Test
  @DisplayName("지지선 돌파이후 후보베이스 RESISTANCE가 정상 범위면 confirm 된다")
  void evaluateBase_resistance_confirm() {
    // given
    Stock stock = stockRepository.save(new Stock("삼성전자", "005930"));

    StockLine supportLine = stockLineRepository.save(
        StockLine.support(90_000L, stock)
    );

    StockBase previous = stockBaseService.createCandidate(stock, supportLine);
    previous.confirm(supportLine);
    stockBaseRepository.save(previous);

    StockLine trigger = stockLineRepository.save(
        StockLine.resistance(100_000L, stock)
    );
    StockBase candidate = stockBaseService.createCandidate(stock, trigger);

    // 정상 범위 (이전 지지보다 위)
    StockLine resistance = stockLineRepository.save(
        StockLine.resistance(95_000L, stock)
    );

    // when
    stockBaseService.evaluateBase(stock, resistance);

    // then
    StockBase updated = stockBaseRepository
        .findLastBase(stock.getId(), StockBaseType.CONFIRMED)
        .orElseThrow();

    assertThat(updated.getId()).isEqualTo(candidate.getId());
  }
}
