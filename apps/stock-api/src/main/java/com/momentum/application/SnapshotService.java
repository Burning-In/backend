package com.momentum.application;

import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.SnapshotRepository;
import com.momentum.domain.StockSnapShot;
import com.momentum.domain.stock.Stock;
import com.momentum.domain.stock.StockRegime;
import com.momentum.domain.stock.StockRepository;
import com.momentum.domain.stockcandle.StockCandleRepository;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse.SnapshotListItem;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.support.error.CoreException;
import com.momentum.support.error.ErrorType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SnapshotService {

  private final SnapshotRepository snapshotRepository;
  private final StockRepository stockRepository;
  private final StockCandleRepository stockCandleRepository;

  @Transactional
  public SnapshotCreateResponse create(SnapshotCreateRequest request) {
    Stock stock = stockRepository.findById(request.stockId())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "종목을 찾을 수 없습니다: " + request.stockId()));

    long capturedPrice = stockCandleRepository.findRecentCandle(stock, LocalDate.now())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "최신 캔들을 찾을 수 없습니다: " + request.stockId()))
        .getClosePrice();

    List<StockSnapShot> references = snapshotRepository.findAllByIds(request.referenceSnapshotIds());
    LocalDateTime recordedAt = LocalDateTime.now();

    StockSnapShot snapshot = snapshotRepository.save(
        StockSnapShot.create(stock, capturedPrice, request.judgment(), references, recordedAt,
            request.retrospective()));

    return new SnapshotCreateResponse(snapshot.getId(), recordedAt.toLocalDate());
  }

  @Transactional(readOnly = true)
  public SnapshotDetailResponse getDetail(Long snapshotId) {
    StockSnapShot snapshot = findSnapshot(snapshotId);
    List<Long> referenceSnapshotIds = snapshot.getReferences().stream()
        .map(reference -> reference.getReferenced().getId())
        .toList();
    return new SnapshotDetailResponse(referenceSnapshotIds, snapshot.getRecordedAt(),
        snapshot.getRetrospective());
  }

  // 추후에 커서 페이지네이션으로 수정필요
  @Transactional(readOnly = true)
  public SnapshotListResponse getSnapShots(LocalDateTime startDate, LocalDateTime endDate,
      List<SnapshotJudgment> judgments, List<StockRegime> regimes, String stockName) {
    List<StockSnapShot> snapshots =
        snapshotRepository.search(startDate, endDate, judgments, regimes, stockName);

    List<SnapshotListItem> items = snapshots.stream()
        .map(snapshot -> new SnapshotListItem(
            snapshot.getId(),
            snapshot.getStock().getName(),
            snapshot.getCapturedRegime(),
            snapshot.getJudgment(),
            snapshot.getRecordedAt(),
            snapshot.getCapturedPrice()))
        .toList();

    return new SnapshotListResponse(
        countOf(snapshots, SnapshotJudgment.BUY),
        countOf(snapshots, SnapshotJudgment.SELL),
        countOf(snapshots, SnapshotJudgment.WATCH),
        items);
  }

  @Transactional
  public void update(Long snapshotId, SnapshotUpdateRequest request) {
    StockSnapShot snapshot = findSnapshot(snapshotId);
    List<StockSnapShot> references = snapshotRepository.findAllByIds(request.referenceSnapshotIds());
    snapshot.update(request.judgment(), references, request.retrospective());
    snapshotRepository.save(snapshot);
  }

  private StockSnapShot findSnapshot(Long snapshotId) {
    return snapshotRepository.findById(snapshotId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "스냅샷을 찾을 수 없습니다: " + snapshotId));
  }

  private int countOf(List<StockSnapShot> snapshots, SnapshotJudgment judgment) {
    return (int) snapshots.stream().filter(snapshot -> snapshot.getJudgment() == judgment).count();
  }
}
