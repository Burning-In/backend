package com.momentum.domain.auth;

import java.util.Optional;

public interface RefreshTokenRepository {

  RefreshToken save(RefreshToken refreshToken);

  Optional<RefreshToken> findActiveByToken(String token);
}
