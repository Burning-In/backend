package com.momentum.application;

import com.momentum.infrastructure.StockTickInfo;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockRegimeRealtimeFacade {

  public static final Double NOISE_THRESHOLD_PERCENT = 2.0;

  @Transactional
  public void resolveRealtimeRegime(StockTickInfo stockTickInfo, Instant now) {
    // # 돌파시작

    // # 돌파준비 -> 이걸 실시간으로 해야되나?

    // # 돌파실패

  }
}
