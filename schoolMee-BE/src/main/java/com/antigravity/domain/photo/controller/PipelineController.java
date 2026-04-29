package com.antigravity.domain.photo.controller;

import com.antigravity.domain.photo.dto.PipelineRequest;
import com.antigravity.domain.photo.dto.PipelineStatusResponse;
import com.antigravity.domain.photo.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사진 처리 통합 파이프라인 API (비동기 처리).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/pipeline")
@Tag(name = "04. Pipeline", description = "분석 + 매칭 + 자동 스토리 생성 연결 파이프라인 (비동기)")
public class PipelineController {

    private final PipelineService pipelineService;

    @Operation(
            summary = "비동기 통합 파이프라인 시작",
            description = "배열로 된 photoIds와 schoolId를 받아 비동기로 파이프라인을 실행하고 jobId를 반환합니다."
    )
    @PostMapping("/process")
    public ResponseEntity<PipelineStatusResponse> process(
            @RequestBody PipelineRequest request) {
        return ResponseEntity.ok(pipelineService.startPipeline(request.getPhotoIds(), request.getSchoolId()));
    }

    @Operation(
            summary = "파이프라인 작업 상태 조회",
            description = "jobId를 통해 현재 파이프라인 처리 상태(PENDING, PROCESSING, SUCCEEDED, FAILED)를 조회합니다."
    )
    @GetMapping("/status/{jobId}")
    public ResponseEntity<PipelineStatusResponse> getStatus(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(pipelineService.getJobStatus(jobId));
    }
}
