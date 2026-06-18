package com.momentum.domain.auth;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refresh_token", indexes = @Index(name = "idx_refresh_token_token", columnList = "token"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

  private Long memberId;

  @Column(length = 512)
  private String token;

  private Instant expiresAt;

  private RefreshToken(Long memberId, String token, Instant expiresAt) {
    this.memberId = memberId;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public static RefreshToken issue(Long memberId, String token, Instant expiresAt) {
    return new RefreshToken(memberId, token, expiresAt);
  }
}
