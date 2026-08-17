package com.momentum.domain.like;

import com.momentum.domain.BaseEntity;
import com.momentum.domain.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "stock_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  private Long stockId;

  private StockLike(Member member, Long stockId) {
    this.member = member;
    this.stockId = stockId;
  }

  public static StockLike create(Member member, Long stockId) {
    return new StockLike(member, stockId);
  }

  @Override
  protected void guard() {
    if (member == null || stockId == null) {
      throw new IllegalArgumentException("회원과 종목은 비어있을 수 없습니다.");
    }
  }
}
