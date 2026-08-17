package com.momentum.domain.like;

import com.momentum.domain.member.Member;
import java.util.List;
import java.util.Optional;

public interface StockLikeRepository {

  StockLike save(StockLike stockLike);

  Optional<StockLike> findByMemberAndStockId(Member member, Long stockId);

  List<StockLike> findAllByMember(Member member);

  void delete(StockLike stockLike);
}
