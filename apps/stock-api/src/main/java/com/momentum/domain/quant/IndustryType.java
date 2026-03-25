package com.momentum.domain.quant;

import lombok.Getter;

@Getter
public enum IndustryType {
  ELECTRONICS("전기전자"),
  SEMICONDUCTOR("반도체"),
  IT_SOFTWARE("IT/소프트웨어"),
  AUTOMOBILE("자동차"),
  CHEMICAL("화학"),
  BIO_HEALTHCARE("바이오/헬스케어"),
  FINANCIAL("금융"),
  CONSTRUCTION("건설"),
  STEEL("철강"),
  ENERGY("에너지"),
  RETAIL("유통"),
  TELECOMMUNICATION("통신"),
  ENTERTAINMENT("엔터"),
  TRANSPORTATION("운송"),
  FOOD_BEVERAGE("식음료");

  private final String description;

  IndustryType(String description) {
    this.description = description;
  }
}
