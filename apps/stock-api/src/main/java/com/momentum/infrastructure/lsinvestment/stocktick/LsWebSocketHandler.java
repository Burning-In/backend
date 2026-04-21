package com.momentum.infrastructure.lsinvestment.stocktick;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.application.dto.StockTickInfo;
import com.momentum.domain.entity.stock.StockCode;
import com.momentum.infrastructure.lsinvestment.dto.stocktick.LsWsRequest;
import com.momentum.infrastructure.lsinvestment.dto.stocktick.LsWsResponse;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
//@Component
@RequiredArgsConstructor
public class LsWebSocketHandler extends TextWebSocketHandler {

  @Value("${ls-investment.auth-token}")
  private String authToken;

  private final ObjectMapper objectMapper;

  private volatile WebSocketSession currentSession;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    log.info("LS 웹소켓 연결 성공");

    this.currentSession = session;

    subscribeAll();
  }

  private void subscribeAll() throws Exception {

    for (StockCode stock : StockCode.values()) {

      LsWsRequest request = LsWsRequest.subscribe(
          authToken,
          stock.getCode()
      );

      String payload = objectMapper.writeValueAsString(request);
      currentSession.sendMessage(new TextMessage(payload));

      log.info("subscribe {}", stock);

      Thread.sleep(100);
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();

    log.info("LS WS RAW MESSAGE = {}", payload);
    try {
      LsWsResponse response = objectMapper.readValue(payload, LsWsResponse.class);
      // subscribe ACK 메시지 패스
      if (response.body() == null) {
        return;
      }
      // dto로 바꿔서 서비스나 db에 저장
      StockTickInfo.from(response);
    } catch (Exception e) {
      log.warn("LS tick parse error payload={}", payload, e);
    }
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

  public synchronized void closeCurrentSession() {

    try {

      if (currentSession == null) {
        return;
      }

      if (!currentSession.isOpen()) {
        return;
      }

      log.info("구독 해제 시작");

      for (StockCode stock : StockCode.values()) {

        LsWsRequest request =
            LsWsRequest.unsubscribe(
                authToken,
                stock.getCode()
            );

        String payload = objectMapper.writeValueAsString(request);

        currentSession.sendMessage(new TextMessage(payload));

        log.info("unsubscribe {}", stock);
      }

      currentSession.close(CloseStatus.NORMAL);

      log.info("웹소켓 정상 종료");

    } catch (Exception e) {

      log.error("웹소켓 종료 실패", e);

    } finally {

      currentSession = null;
    }
  }
}
