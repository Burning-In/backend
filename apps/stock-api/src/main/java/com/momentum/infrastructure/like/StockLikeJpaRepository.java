package com.momentum.infrastructure.like;

import com.momentum.domain.like.StockLike;
import com.momentum.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLikeJpaRepository extends JpaRepository<StockLike, Long> {

  Optional<StockLike> findByMemberAndStockId(Member member, Long stockId);

  List<StockLike> findAllByMemberOrderByCreatedAtDesc(Member member);
}
