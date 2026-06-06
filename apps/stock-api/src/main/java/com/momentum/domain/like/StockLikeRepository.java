package com.momentum.domain.like;

import com.momentum.domain.member.Member;
import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;

public interface StockLikeRepository {

  StockLike save(StockLike stockLike);

  Optional<StockLike> findByMemberAndStock(Member member, Stock stock);

  List<StockLike> findAllByMember(Member member);

  void delete(StockLike stockLike);
}
