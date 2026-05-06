package com.momentum.infrastructure.rs;

import com.momentum.domain.rs.KOSPI;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KOSPIJpaRepository extends JpaRepository<KOSPI, Integer> {

}
