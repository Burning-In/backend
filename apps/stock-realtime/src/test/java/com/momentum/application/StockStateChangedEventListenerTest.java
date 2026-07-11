package com.momentum.application;

import static com.momentum.domain.stock.StockRegime.BREAKOUT_READY;
import static com.momentum.domain.stock.StockRegime.BREAKOUT_SUCCESS;
import static org.mockito.Mockito.verify;

import com.momentum.domain.stock.StockStateChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockStateChangedEventListenerTest {

  @Mock
  private RealtimeRankingFacade realtimeRankingFacade;

  @InjectMocks
  private StockStateChangedEventListener listener;

  @Test
  @DisplayName("레짐 변경 시 빠진 레짐(from)과 들어온 레짐(to) 랭킹을 모두 갱신한다")
  void updatesBothFromAndToRankings() {
    listener.on(new StockStateChangedEvent("000040", BREAKOUT_READY, BREAKOUT_SUCCESS));

    verify(realtimeRankingFacade).updateRanking(BREAKOUT_READY);
    verify(realtimeRankingFacade).updateRanking(BREAKOUT_SUCCESS);
  }
}
