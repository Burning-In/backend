package com.momentum.infrastructure.auth;

import com.momentum.domain.auth.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenAndDeletedAtIsNull(String token);
}
