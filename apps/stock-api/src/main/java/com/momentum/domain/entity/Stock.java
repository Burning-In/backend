package com.momentum.domain.entity;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class Stock extends BaseEntity {

  private String name;
  private String code;
}
