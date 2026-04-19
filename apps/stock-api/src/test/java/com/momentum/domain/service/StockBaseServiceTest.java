package com.momentum.domain.service;

import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.respository.StockLineRepository;
import com.momentum.domain.respository.StockRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class StockBaseServiceTest {

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
  void create_OverBase_Candidate_Candidate_noPreviousBase() {

  }

  @Test
  @DisplayName("저항선 돌파 이후, 후보 베이스 첫번쨰 SUPPORT가 이전 저항보다 낮으면 병합된다")
  void merge_support_merge() {

  }

  @Test
  @DisplayName("저항선 돌파 이후 후보베이스 SUPPORT가 정상 범위면 confirm 된다")
  void merge_support_confirm() {

  }

  // 이건 뭐가 문제지
  @Test
  @DisplayName("지지선 하락돌파이후 후보베이스 RESISTANCE가 이전 지지보다 낮으면 병합된다")
  void merge_resistance_merge() {

  }

  @Test
  @DisplayName("지지선 돌파이후 후보베이스 RESISTANCE가 정상 범위면 confirm 된다")
  void merge_resistance_confirm() {

  }
}
