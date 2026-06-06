package com.momentum.infrastructure.like;

import com.momentum.domain.like.StockLike;
import com.momentum.domain.like.StockLikeRepository;
import com.momentum.domain.member.Member;
import com.momentum.domain.stock.Stock;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockLikeRepositoryImpl implements StockLikeRepository {

  private final StockLikeJpaRepository stockLikeJpaRepository;

  @Override
  public StockLike save(StockLike stockLike) {
    return stockLikeJpaRepository.save(stockLike);
  }

  @Override
  public Optional<StockLike> findByMemberAndStock(Member member, Stock stock) {
    return stockLikeJpaRepository.findByMemberAndStock(member, stock);
  }

  @Override
  public List<StockLike> findAllByMember(Member member) {
    return stockLikeJpaRepository.findAllByMemberWithStock(member);
  }

  @Override
  public void delete(StockLike stockLike) {
    stockLikeJpaRepository.delete(stockLike);
  }
}
