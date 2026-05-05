package com.momentum.interfaces.api.snapshot;

import com.momentum.interfaces.api.ApiResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.CompletedDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.OngoingDetailResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCompleteRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateRequest;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotCreateResponse;
import com.momentum.interfaces.api.snapshot.SnapshotV1Dto.SnapshotListResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
        @RequestParam(defaultValue = "true") boolean includeOngoing,
        @RequestParam(defaultValue = "true") boolean includeCompleted,
        @RequestParam(defaultValue = "false") boolean breakoutStart,
        @RequestParam(defaultValue = "false") boolean breakoutReady,
        @RequestParam(defaultValue = "false") boolean breakoutFailed,
        @RequestParam(defaultValue = "false") boolean downsideBreak,
        @RequestParam(defaultValue = "false") boolean undetermined,
        @RequestParam(defaultValue = "false") boolean momentum,
        @RequestParam(defaultValue = "false") boolean fip,
        @RequestParam(defaultValue = "false") boolean movingAverage,
        @RequestParam(defaultValue = "false") boolean volume,
        @RequestParam(defaultValue = "false") boolean eps,
        @RequestParam(defaultValue = "false") boolean rs,
        @RequestParam(defaultValue = "RECENT") String sort
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

    @PatchMapping("/{snapshotId}/complete")
    @Override
    public ApiResponse<Void> completeSnapshot(
        @PathVariable Long snapshotId,
        @RequestBody SnapshotCompleteRequest request
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

    // ===================== Snapshot Ongoing Detail Page =====================

    @GetMapping("/{snapshotId}/ongoing")
    @Override
    public ApiResponse<OngoingDetailResponse> getOngoingDetail(
        @PathVariable Long snapshotId
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

    // ===================== Snapshot Completed Detail Page =====================

    @GetMapping("/{snapshotId}/completed")
    @Override
    public ApiResponse<CompletedDetailResponse> getCompletedDetail(
        @PathVariable Long snapshotId
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }

    // ===================== 공통 =====================

    @DeleteMapping("/{snapshotId}")
    @Override
    public ApiResponse<Void> deleteSnapshot(
        @PathVariable Long snapshotId
    ) {
        // TODO: SnapshotFacade 연결
        return ApiResponse.success(null);
    }
}
