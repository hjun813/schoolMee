package com.antigravity.domain.photo.controller;

import com.antigravity.domain.photo.dto.PhotoAnalysisResponse;
import com.antigravity.domain.photo.dto.PhotoMatchResponse;
import com.antigravity.domain.photo.dto.PhotoUploadResponse;
import com.antigravity.domain.photo.service.PhotoAnalysisService;
import com.antigravity.domain.photo.service.PhotoMatchService;
import com.antigravity.domain.photo.service.PhotoUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사진 처리 API (단계별 개별 호출).
 *
 * [호출 순서]
 * 1. POST /api/v1/photos/upload   → 사진 업로드 (PENDING)
 * 2. POST /api/v1/photos/analyze  → AI 분석 시뮬레이션 (ANALYZED)
 * 3. POST /api/v1/photos/match    → 학생 매칭 (PhotoStudent 생성)
 * 4. POST /api/v1/admin/stories/generate?schoolId={id}  → Story 생성 (기존 API)
 *
 * 한 번에 처리: POST /api/v1/pipeline/process
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photos")
@Tag(name = "03. Photo Pipeline", description = "사진 업로드 / AI 분석 / 학생 매칭 (단계별)")
public class PhotoController {

    private final PhotoUploadService photoUploadService;
    private final PhotoAnalysisService photoAnalysisService;
    private final PhotoMatchService photoMatchService;

    @Operation(
            summary = "① 사진 업로드",
            description = "학교 사진 풀에 사진을 업로드합니다. 업로드된 사진은 PENDING 상태로 저장됩니다. " +
                          "다음 단계: POST /api/v1/photos/analyze?schoolId={schoolId}"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponse> upload(
            @Parameter(description = "학교 ID") @RequestParam Long schoolId,
            @Parameter(description = "업로드할 사진 파일 (복수 가능)")
            @RequestPart("files") MultipartFile[] files) {
        return ResponseEntity.ok(photoUploadService.uploadPhotos(schoolId, files));
    }

    @Operation(
            summary = "② 사진 AI 분석 (시뮬레이션)",
            description = "학교의 PENDING 상태 사진을 AI 시뮬레이션으로 분석합니다. " +
                          "smile_score, activity_score, detectedFacesCount, faceIds가 생성되고 ANALYZED 상태가 됩니다. " +
                          "다음 단계: POST /api/v1/photos/match?schoolId={schoolId}"
    )
    @PostMapping("/analyze")
    public ResponseEntity<PhotoAnalysisResponse> analyze(
            @Parameter(description = "학교 ID") @RequestParam Long schoolId) {
        return ResponseEntity.ok(photoAnalysisService.analyzeAllPending(schoolId));
    }

    @Operation(
            summary = "③ 사진-학생 매칭",
            description = "ANALYZED 사진과 학생을 매칭하여 PhotoStudent 관계를 생성합니다. " +
                          "각 사진의 detectedFacesCount만큼 학생이 랜덤 매칭됩니다. " +
                          "다음 단계: POST /api/v1/admin/stories/generate?schoolId={schoolId}"
    )
    @PostMapping("/match")
    public ResponseEntity<PhotoMatchResponse> match(
            @Parameter(description = "학교 ID") @RequestParam Long schoolId) {
        return ResponseEntity.ok(photoMatchService.matchStudents(schoolId));
    }
}
