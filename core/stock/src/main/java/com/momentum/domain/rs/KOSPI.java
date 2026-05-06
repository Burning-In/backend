package com.momentum.domain.rs;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KOSPI extends BaseEntity {

  @Column(name = "kospi_value")
  private Long value;
  private LocalDate recordDate;

  public KOSPI(Long value, LocalDate recordDate) {
    this.value = value;
    this.recordDate = recordDate;
  }
}
