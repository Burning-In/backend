package com.momentum.application;

import com.momentum.domain.like.StockLike;
import com.momentum.domain.like.StockLikeRepository;
import com.momentum.domain.member.Member;
import com.momentum.domain.member.MemberRepository;
import com.momentum.infrastructure.query.StockIdentityRow;
import com.momentum.infrastructure.query.StockMetaQueryDao;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse;
import com.momentum.interfaces.api.stock.StockLikeV1Dto.LikeStockResponse.LikeStockItem;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockLikeService {

  private final StockLikeRepository stockLikeRepository;
  private final MemberRepository memberRepository;
  private final StockMetaQueryDao stockMetaQueryDao;

  @Transactional
  public void addLike(Long memberId, String stockCode) {
    Member member = findMember(memberId);
    Long stockId = findStockId(stockCode);

    if (stockLikeRepository.findByMemberAndStockId(member, stockId).isPresent()) {
      return;
    }
    stockLikeRepository.save(StockLike.create(member, stockId));
  }

  @Transactional
  public void removeLike(Long memberId, String stockCode) {
    Member member = findMember(memberId);
    Long stockId = findStockId(stockCode);

    // 관심 종목이 아니면 멱등하게 무시한다.
    stockLikeRepository.findByMemberAndStockId(member, stockId)
        .ifPresent(stockLikeRepository::delete);
  }

  @Transactional(readOnly = true)
  public LikeStockResponse getLikeStocks(Long memberId) {
    Member member = findMember(memberId);

    List<Long> likedStockIds = stockLikeRepository.findAllByMember(member).stream()
        .map(StockLike::getStockId)
        .toList();
    Map<Long, StockIdentityRow> stocks = stockMetaQueryDao.findIdentitiesByIds(likedStockIds);

    List<LikeStockItem> items = likedStockIds.stream()
        .map(stocks::get)
        .flatMap(stock -> Optional.ofNullable(stock).stream())
        .map(stock -> new LikeStockItem(stock.stockCode(), stock.stockName()))
        .toList();

    return new LikeStockResponse(items);
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "회원을 찾을 수 없습니다: " + memberId));
  }

  private Long findStockId(String stockCode) {
    return stockMetaQueryDao.findIdentityByCode(stockCode)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + stockCode))
        .stockId();
  }
}
