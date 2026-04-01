package com.momentum.domain.service.impl;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.StockBase;
import com.momentum.domain.entity.indicator.StockLine;
import com.momentum.domain.respository.StockBaseRepository;
import com.momentum.domain.service.StockBaseService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockBaseServiceImpl implements StockBaseService {

  private final StockBaseRepository stockBaseRepository;

  // 애는 기존 베이스에서 벗어난 저항선 또는 지지선
  // # 예외처리 : triggerLine이 이전베이스에 정말 존재하지 않던, 초과미만의 라인인가?
  public StockBase createCandidate(Stock stock, StockLine triggerLine) {
    Optional<StockBase> previousBase = stockBaseRepository.findLastBase(stock.getId());
    long previousBaseAccCount = previousBase
        .map(StockBase::getAccumulationCount)
        .orElse(0L);
    StockBase candidate = StockBase.createCandidate(stock, triggerLine, previousBaseAccCount);

    return stockBaseRepository.save(candidate);
  }

  // 새로운 저항선 -> 후보 베이스 -> 처음 저점 -> 이전베이스 지지선 아래 형성되면 베이스 병합
  // 새로운 지지선 -> 후보베이스  -> 처음 고점 -> 이전베이스 지지선 위에 형성되면 베이스 병합


  // # 베이스 내부의 변동성 업데이트에 사용
  // - 베이스 지지/저항내에 피봇이 있으면 변동성 업데이트에 사용이 됩니다. -> 지지/저항으로 생성되지는 않습니다.
  public StockBase update(Stock stock, StockCandle pivotCandle) {
    return null;
  }

  // # 새로운 저항선 이후, 새로운 candidate이후로 첫저점이 지지선아래, 지지/저항선 이전 베이스에 병합하고, candidate 삭제
  // # 돌파이후 새로운 candidate이후로 첫저점이 나온다면, 지지/저항선 이전 베이스에 병합하고  candidate 삭제
  public StockBase merge(Stock stock, StockLine stockLine) {
    return null;
  }

  // # 병합이후 삭제
  public StockBase delete(Stock stock, StockLine stockLine) {
    return null;
  }
}

//	1.	이전 베이스 없으면 → 고점/저점 상관없이 바로 베이스 생성
//	2.	이전 저항 돌파 시 → 바로 확정 X, candidate 베이스로 먼저 생성
//	3.	돌파 이후 첫 저점 기준으로 판단 → 이전 지지보다 높으면 새로운 베이스 확정
//	4.	조건 안 맞으면 → 기존 베이스로 merge하고 candidate 제거
//	5.	핵심 규칙 → “고점은 후보, 저점이 나와야 베이스 확정”
