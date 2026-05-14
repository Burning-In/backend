package com.momentum.interfaces.api.snapshot;

import com.momentum.domain.stock.StockRegime;
import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotUpdateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotJudgment;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import java.time.LocalDate;
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

    // ===================== Snapshot List Page =====================

    @GetMapping
    @Override
    public ApiResponse<SnapshotListResponse> getSnapshotList(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) List<SnapshotJudgment> judgments,
        @RequestParam(required = false) List<StockRegime> regimes,
        @RequestParam(required = false) String stockName
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

    // ===================== Snapshot Detail =====================

    @GetMapping("/{snapshotId}")
    @Override
    public ApiResponse<SnapshotDetailResponse> getSnapshotDetail(
        @PathVariable Long snapshotId
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

    // ===================== Snapshot Editor Overlay =====================

    @PostMapping
    @Override
    public ApiResponse<SnapshotCreateResponse> createSnapshot(
        @RequestBody SnapshotCreateRequest request
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

    @PatchMapping("/{snapshotId}")
    @Override
    public ApiResponse<Void> updateSnapshot(
        @PathVariable Long snapshotId,
        @RequestBody SnapshotUpdateRequest request
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

}
