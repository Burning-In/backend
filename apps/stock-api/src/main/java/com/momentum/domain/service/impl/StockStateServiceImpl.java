package com.momentum.domain.service.impl;

import com.momentum.domain.service.StockStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockStateServiceImpl implements StockStateService {
  // 저항/지지로 주식 상태 정하는로직

  // 상승돌파 : 들어온 데이터가 기존 저항선을 돌파하면,
  // 상승가능 : 변동성 감소, 저항선 부근 왔다갔다(실시간)
  // 하락 가능 : 저항에 부딪치고, 돌파 실패후 하락
  // 하락 붕괴 : 지지선 이탈, 일단, 베이스의 최저지지 하락할떄 이탈


  // 그니깐 저장을 하되, 이벤트랑 Raw틱 구분해서 저장하라는거구나 -> 이게 맞다 ㄹㅇ, 일단 분석에 한번 사용해보자

  // # 실시간 데이터 -> 애는 실시간으로 받는거고, 들어온 데이터가 저앟ㅇ선을 넘었는지 아니면 어떻게 안넘었는지 보는거고
  //  tick 수신
  // → evaluate(price)
  // → 상태 overwrite
  //  이전 상태와 다르면 이벤트 기록
  // → (노이즈라면 이벤트 생략 가능)  -> 노이즈의 기준은 2%정도?

  // # 일봉 데이터(보정 및 확정용)
  //  종가 수신
  // → evaluate(closePrice)
  // → 상태 overwrite (최종 확정)
  // → 이벤트 무조건 기록 (CONFIRMED)
  // → 구조 변경 (저항/지지/베이스)

}
