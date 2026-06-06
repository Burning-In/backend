package com.momentum.application;

import com.momentum.domain.like.StockLike;
import com.momentum.domain.like.StockLikeRepository;
import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRepository;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse.LikeStockItem;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockLikeService {

  private final StockLikeRepository stockLikeRepository;
  private final MemberRepository memberRepository;
  private final StockRepository stockRepository;

  @Transactional
  public void addLike(Long memberId, String stockCode) {
    Member member = findMember(memberId);
    Stock stock = findStock(stockCode);

    if (stockLikeRepository.findByMemberAndStock(member, stock).isPresent()) {
      return;
    }
    stockLikeRepository.save(StockLike.create(member, stock));
  }

  @Transactional
  public void removeLike(Long memberId, String stockCode) {
    Member member = findMember(memberId);
    Stock stock = findStock(stockCode);

    // 관심 종목이 아니면 멱등하게 무시한다.
    stockLikeRepository.findByMemberAndStock(member, stock)
        .ifPresent(stockLikeRepository::delete);
  }

  @Transactional(readOnly = true)
  public LikeStockResponse getLikeStocks(Long memberId) {
    Member member = findMember(memberId);

    List<LikeStockItem> items = stockLikeRepository.findAllByMember(member).stream()
        .map(StockLike::getStock)
        .map(stock -> new LikeStockItem(stock.getCode(), stock.getName()))
        .toList();

    return new LikeStockResponse(items);
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다: " + memberId));
  }

  private Stock findStock(String stockCode) {
    return stockRepository.findByStockCode(stockCode)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockCode));
  }
}
