package com.momentum.infrastructure.like;

import com.momentum.domain.like.StockLike;
import com.momentum.domain.member.Member;
import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockLikeJpaRepository extends JpaRepository<StockLike, Long> {

  Optional<StockLike> findByMemberAndStock(Member member, Stock stock);

  @Query("SELECT sl FROM StockLike sl JOIN FETCH sl.stock WHERE sl.member = :member ORDER BY sl.createdAt DESC")
  List<StockLike> findAllByMemberWithStock(@Param("member") Member member);
}
