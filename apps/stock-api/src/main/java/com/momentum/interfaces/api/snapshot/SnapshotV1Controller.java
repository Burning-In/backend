package com.momentum.interfaces.api.snapshot;

import com.momentum.application.SnapshotService;
import com.momentum.domain.SnapshotJudgment;
import com.momentum.domain.stock.StockRegime;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/snapshots")
public class SnapshotV1Controller implements SnapshotV1ApiSpec {

    private final SnapshotService snapshotService;

    // ===================== Snapshot List Page =====================

    @GetMapping
    @Override
    public ApiResponse<SnapshotListResponse> getSnapshotList(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(required = false) List<SnapshotJudgment> judgments,
        @RequestParam(required = false) List<StockRegime> regimes,
        @RequestParam(required = false) String stockName
    ) {
        return ApiResponse.success(
            snapshotService.getSnapShots(startDate, endDate, judgments, regimes, stockName));
    }

    // ===================== Snapshot Detail =====================

    @GetMapping("/{snapshotId}")
    @Override
    public ApiResponse<SnapshotDetailResponse> getSnapshotDetail(
        @PathVariable Long snapshotId
    ) {
        return ApiResponse.success(snapshotService.getDetail(snapshotId));
    }

    // ===================== Snapshot Editor Overlay =====================

    @PostMapping
    @Override
    public ApiResponse<SnapshotCreateResponse> createSnapshot(
        @RequestBody SnapshotCreateRequest request
    ) {
        return ApiResponse.success(snapshotService.create(request));
    }

    @PatchMapping("/{snapshotId}")
    @Override
    public ApiResponse<Void> updateSnapshot(
        @PathVariable Long snapshotId,
        @RequestBody SnapshotUpdateRequest request
    ) {
        snapshotService.update(snapshotId, request);
        return ApiResponse.success(null);
    }

}
