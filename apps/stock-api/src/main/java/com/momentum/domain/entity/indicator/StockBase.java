package com.momentum.domain.entity.indicator;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockBase extends BaseEntity {

  private double highPrice;
  private double lowPrice;
  private int currentCount;
}
