package com.momentum.domain;

import jakarta.persistence.Entity;

@Entity
public class Stock extends BaseEntity {

  private String name;
  private String code;
}
