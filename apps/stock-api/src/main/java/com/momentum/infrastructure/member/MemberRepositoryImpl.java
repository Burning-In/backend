package com.momentum.infrastructure.member;

import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberRepositoryImpl implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;

  @Override
  public Member save(Member member) {
    return memberJpaRepository.save(member);
  }

  @Override
  public Optional<Member> findById(Long id) {
    return memberJpaRepository.findById(id);
  }

  @Override
  public Optional<Member> findByEmail(String email) {
    return memberJpaRepository.findByEmail(email);
  }

  @Override
  public Optional<Member> findByPhoneNumberAndName(String phoneNumber, String name) {
    return memberJpaRepository.findByPhoneNumberAndName(phoneNumber, name);
  }

  @Override
  public Optional<Member> findByEmailAndName(String email, String name) {
    return memberJpaRepository.findByEmailAndName(email, name);
  }

  @Override
  public Optional<Member> findByEmailAndNameAndPhoneNumber(String email, String name,
      String phoneNumber) {
    return memberJpaRepository.findByEmailAndNameAndPhoneNumber(email, name, phoneNumber);
  }
}
