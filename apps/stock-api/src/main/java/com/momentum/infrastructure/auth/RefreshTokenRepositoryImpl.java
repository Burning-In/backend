package com.momentum.infrastructure.auth;

import com.momentum.domain.auth.RefreshToken;
import com.momentum.domain.auth.RefreshTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

  private final RefreshTokenJpaRepository refreshTokenJpaRepository;

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    return refreshTokenJpaRepository.save(refreshToken);
  }

  @Override
  public Optional<RefreshToken> findActiveByToken(String token) {
    return refreshTokenJpaRepository.findByTokenAndDeletedAtIsNull(token);
  }
}
