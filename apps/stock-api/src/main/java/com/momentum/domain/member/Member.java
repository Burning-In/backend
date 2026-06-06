package com.momentum.domain.member;

import com.momentum.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Column(unique = true)
  private String email;

  private String password;

  private String nickname;

  private String name;

  private String phoneNumber;

  private Member(String email, String password, String nickname, String name, String phoneNumber) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  public static Member create(String email, String password, String nickname, String name,
      String phoneNumber) {
    return new Member(email, password, nickname, name, phoneNumber);
  }

  @Override
  protected void guard() {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
    }
  }
}
