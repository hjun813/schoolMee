package com.antigravity.domain.photo.controller;

import com.antigravity.domain.photo.dto.PipelineResponse;
import com.antigravity.domain.photo.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사진 처리 통합 파이프라인 API.
 * 업로드 + AI 분석 + 학생 매칭을 한 번의 요청으로 처리한다.
 *
 * Story 생성은 POST /api/v1/admin/stories/generate?schoolId={id} 로 별도 호출.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pipeline")
@Tag(name = "04. Pipeline", description = "업로드 + 분석 + 매칭 통합 파이프라인")
public class PipelineController {

    private final PipelineService pipelineService;

    @Operation(
            summary = "통합 파이프라인 처리 (업로드 → 분석 → 매칭)",
            description = "사진 업로드, AI 분석 시뮬레이션, 학생 매칭을 한 번에 처리합니다.\n\n" +
                          "**Story 생성은 포함되지 않습니다.** " +
                          "파이프라인 완료 후 POST /api/v1/admin/stories/generate?schoolId={schoolId} 를 별도로 호출하세요."
    )
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PipelineResponse> process(
            @Parameter(description = "학교 ID") @RequestParam Long schoolId,
            @Parameter(description = "업로드할 사진 파일들")
            @RequestPart("files") MultipartFile[] files) {
        return ResponseEntity.ok(pipelineService.processPipeline(schoolId, files));
    }
}
