package com.momentum.domain.member;

import java.util.Optional;

public interface MemberRepository {

  Member save(Member member);

  Optional<Member> findById(Long id);

  Optional<Member> findByEmail(String email);

  Optional<Member> findByPhoneNumberAndName(String phoneNumber, String name);

  Optional<Member> findByEmailAndName(String email, String name);

  Optional<Member> findByEmailAndNameAndPhoneNumber(String email, String name, String phoneNumber);
}
