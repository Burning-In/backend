package com.momentum.infrastructure.lsinvestment.stocktick;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class LsWebSocketClient {

  private static final String WS_URL =
      "wss://openapi.ls-sec.co.kr:9443/websocket";

  private final LsWebSocketHandler handler;
  private final StandardWebSocketClient client;

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();

  private volatile WebSocketSession session;

  @PostConstruct
  public void connect() {

    try {
      handler.closeCurrentSession();

      CompletableFuture<WebSocketSession> future =
          client.execute(
              handler,
              new WebSocketHttpHeaders(),
              URI.create(WS_URL)
          );

      session = future.get();

      log.info("LS 웹소켓 연결 성공");

    } catch (Exception e) {
      log.error("웹소켓 연결 실패", e);

      reconnect();
    }
  }

  public void reconnect() {

    log.warn("웹소켓 재연결 시도 (3초 후)");

    scheduler.schedule(
        this::connect,
        3,
        TimeUnit.SECONDS
    );
  }

  public void close() {

    try {

      if (session != null && session.isOpen()) {
        session.close();
      }

    } catch (Exception e) {

      log.error("웹소켓 종료 실패", e);

    }
  }

  @Scheduled(fixedDelay = 10000)
  public void healthCheck() {

    if (session == null || !session.isOpen()) {

      log.warn("웹소켓 연결 끊김 감지 → reconnect");

      reconnect();
    }
  }
}
