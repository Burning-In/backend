package com.momentum.infrastructure.example;

import com.momentum.domain.example.ExampleModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleJpaRepository extends JpaRepository<ExampleModel, Long> {}
