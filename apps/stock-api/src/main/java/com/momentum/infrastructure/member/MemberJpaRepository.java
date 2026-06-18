package com.momentum.infrastructure.member;

import com.momentum.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  Optional<Member> findByPhoneNumberAndName(String phoneNumber, String name);

  Optional<Member> findByEmailAndName(String email, String name);

  Optional<Member> findByEmailAndNameAndPhoneNumber(String email, String name, String phoneNumber);
}
