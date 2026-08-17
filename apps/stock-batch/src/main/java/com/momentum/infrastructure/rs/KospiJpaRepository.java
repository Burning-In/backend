package com.momentum.infrastructure.rs;

import com.momentum.domain.relativestrength.Kospi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KospiJpaRepository extends JpaRepository<Kospi, Integer> {

}
