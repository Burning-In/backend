package com.momentum.infrastructure.config;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketClientConfig {

  @Bean
  public StandardWebSocketClient webSocketClient() {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    // 메시지 버퍼 증가
    container.setDefaultMaxTextMessageBufferSize(1024 * 1024 * 5); // 5MB
    container.setDefaultMaxBinaryMessageBufferSize(1024 * 1024 * 5);

    return new StandardWebSocketClient(container);
  }
}
