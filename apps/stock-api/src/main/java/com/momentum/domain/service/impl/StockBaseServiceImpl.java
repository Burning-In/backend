package com.momentum.domain.service.impl;

import com.momentum.domain.entity.Stock;
import com.momentum.domain.entity.StockCandle;
import com.momentum.domain.entity.indicator.StockBase;
import com.momentum.domain.entity.indicator.StockLine;
import com.momentum.domain.service.StockBaseService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class StockBaseServiceImpl implements StockBaseService {

  // # 이전베이스가 없으면 바로 생성
  // # 이전베이스가 있어도 돌파 이후의 고점(저항선)은 새로운 candidate로 생성 -> 이전 베이스 돌파여부를 알아야함
  public StockBase createCandidate(Stock stock, StockLine stockLine) {
    return null;
  }

  // # 베이스 내부의 변동성 업데이트에 사용
  // - 베이스 지지/저항내에 피봇이 있으면 변동성 업데이트에 사용이 됩니다. -> 지지/저항으로 생성되지는 않습니다.
  public StockBase update(Stock stock, StockCandle candle) {
    return null;
  }

  // # 돌파이후 새로운 candidate이후로 첫저점이 지지선아래, 지지/저항선 이전 베이스에 병합하고, candidate 삭제
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
