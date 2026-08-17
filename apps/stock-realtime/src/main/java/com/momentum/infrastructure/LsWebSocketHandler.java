package com.momentum.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.application.dto.StockTickInfo;
import com.momentum.application.StockRealtimeFacade;
import com.momentum.application.StockRealtimeRegimeService;
import com.momentum.infrastructure.query.StockSubscriptionQueryDao;
import com.momentum.infrastructure.query.StockTickWriteDao;
import com.momentum.infrastructure.query.SubscriptionStockRow;
import jakarta.annotation.PreDestroy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class LsWebSocketHandler extends TextWebSocketHandler {

  private static final DateTimeFormatter TRADE_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");

  @Value("${ls-investment.auth-token}")
  private String authToken;

  private final ObjectMapper objectMapper;

  private final StockRealtimeRegimeService stockRealtimeRegimeService;
  private final StockRealtimeFacade stockRealtimeFacade;
  private final StockSubscriptionQueryDao stockSubscriptionQueryDao;
  private final StockTickWriteDao stockTickWriteDao;

  private WebSocketSession currentSession;
  private List<SubscriptionStockRow> subscribedStocks = List.of();


  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("LS 웹소켓 연결 성공");

    this.currentSession = session;

    subscribeAll();
  }

  private void subscribeAll() throws Exception {
    subscribedStocks = stockSubscriptionQueryDao.findAll();
    log.info("구독 대상 종목 {}건", subscribedStocks.size());

    for (SubscriptionStockRow stock : subscribedStocks) {

      LsWsRequest request = LsWsRequest.subscribe(
          authToken,
          stock.stockCode()
      );

      String payload = objectMapper.writeValueAsString(request);
      currentSession.sendMessage(new TextMessage(payload));

      log.info("subscribe {}", stock.stockName());

      Thread.sleep(100);
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();

    log.info("LS WS RAW MESSAGE = {}", payload);
    try {
      LsWsResponse response = objectMapper.readValue(payload, LsWsResponse.class);
      if (response.body() == null) {
        return;
      }
      StockTickInfo tickInfo = StockTickInfo.from(response);
      saveTick(tickInfo);
      stockRealtimeFacade.broadcast(tickInfo);
      stockRealtimeRegimeService.resolveRealtimeRegime(tickInfo.stockCode(), tickInfo.currentPrice());
    } catch (Exception e) {
      log.warn("LS tick parse error payload={}", payload, e);
    }
  }

  private void saveTick(StockTickInfo tickInfo) {
    LocalTime tradeTime = LocalTime.parse(tickInfo.tradeTime(), TRADE_TIME_FORMAT);
    stockTickWriteDao.insert(
        tickInfo.stockCode(),
        LocalDate.now().atTime(tradeTime),
        tickInfo.currentPrice(),
        tickInfo.tradeVolume(),
        tickInfo.accumulatedVolume());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    log.warn("웹소켓 연결 종료: {}", status);

    currentSession = null;
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {

    log.error("웹소켓 에러 발생", exception);

    currentSession = null;
  }

  @PreDestroy
  public void shutdown() {

    log.info("애플리케이션 종료 - 웹소켓 정리");

    closeCurrentSession();
  }

  // 여기도 매개변수로 받아서 해야함
  public synchronized void closeCurrentSession() {

    try {

      if (currentSession == null) {
        return;
      }

      if (!currentSession.isOpen()) {
        return;
      }

      log.info("구독 해제 시작");

      for (SubscriptionStockRow stock : subscribedStocks) {

        LsWsRequest request =
            LsWsRequest.unsubscribe(
                authToken,
                stock.stockCode()
            );

        String payload = objectMapper.writeValueAsString(request);

        currentSession.sendMessage(new TextMessage(payload));

        log.info("unsubscribe {}", stock.stockName());
      }

      subscribedStocks = List.of();

      currentSession.close(CloseStatus.NORMAL);

      log.info("웹소켓 정상 종료");

    } catch (Exception e) {

      log.error("웹소켓 종료 실패", e);

    } finally {

      currentSession = null;
    }
  }
}
