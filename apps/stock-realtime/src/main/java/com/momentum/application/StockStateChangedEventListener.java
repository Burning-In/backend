package com.momentum.application;

import com.momentum.domain.stock.StockStateChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StockStateChangedEventListener {

  private final RealtimeRankingFacade realtimeRankingFacade;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(StockStateChangedEvent event) {
    realtimeRankingFacade.updateRanking(event.fromState());
    realtimeRankingFacade.updateRanking(event.toState());
  }
}
