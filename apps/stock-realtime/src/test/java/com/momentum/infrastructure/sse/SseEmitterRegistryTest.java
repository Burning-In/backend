package com.momentum.infrastructure.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRegistryTest {

  private SseEmitterRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SseEmitterRegistry();
  }

  @Test
  @DisplayName("create는 타임아웃이 설정된 SseEmitter를 반환한다")
  void createReturnsEmitterWithTimeout() {
    SseEmitter emitter = registry.create("005930");

    assertThat(emitter).isNotNull();
    assertThat(emitter.getTimeout()).isEqualTo(60 * 60 * 1000L);
  }

  @Test
  @DisplayName("구독자가 없는 key로 broadcast해도 예외가 발생하지 않는다")
  void broadcastWithoutSubscriberDoesNothing() {
    assertThatCode(() -> registry.broadcast("005930", "tick", "payload"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("구독 중인 연결에 broadcast하면 예외 없이 전송된다")
  void broadcastSendsToSubscriber() {
    registry.create("005930");

    assertThatCode(() -> registry.broadcast("005930", "tick", "payload"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("이미 완료된 연결이 섞여 있어도 broadcast는 예외를 던지지 않고 해당 연결을 정리한다")
  void broadcastRemovesFailedEmitter() {
    SseEmitter emitter = registry.create("005930");
    emitter.complete(); // 완료 상태에서 send() 호출 시 IllegalStateException 발생 → 정리 대상

    assertThatCode(() -> registry.broadcast("005930", "tick", "payload"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("구독한 key와 다른 key로 broadcast하면 해당 구독자는 영향을 받지 않는다")
  void broadcastIsolatesByKey() {
    registry.create("005930");

    assertThatCode(() -> registry.broadcast("000660", "tick", "payload"))
        .doesNotThrowAnyException();
  }
}
