package com.antigravity.domain.photo.service;

import com.antigravity.domain.photo.dto.PhotoAnalysisResponse;
import com.antigravity.domain.photo.dto.PhotoMatchResponse;
import com.antigravity.domain.photo.dto.PhotoUploadResponse;
import com.antigravity.domain.photo.dto.PipelineResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사진 처리 통합 파이프라인 (Facade 패턴).
 *
 * [파이프라인 단계]
 * 1. uploadPhotos()     — 파일 저장 + Photo(PENDING) 엔티티 저장
 * 2. analyzeAllPending() — AI 시뮬레이션으로 ANALYZED 상태로 전환
 * 3. matchStudents()    — ANALYZED 사진과 학생 매칭 (PhotoStudent 저장)
 *
 * Story 생성은 POST /api/v1/admin/stories/generate?schoolId={schoolId} 를 별도 호출.
 * (기존 StoryService.generateStoriesForSchool() 재사용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    private final PhotoUploadService photoUploadService;
    private final PhotoAnalysisService photoAnalysisService;
    private final PhotoMatchService photoMatchService;

    public PipelineResponse processPipeline(Long schoolId, MultipartFile[] files) {
        log.info("=== 파이프라인 시작: schoolId={}, 파일 수={} ===", schoolId, files.length);

        // 1단계: 업로드
        final PhotoUploadResponse uploadResult =
                photoUploadService.uploadPhotos(schoolId, files);
        log.info("[1/3] 업로드 완료: {}장", uploadResult.getUploadedCount());

        // 2단계: AI 분석 시뮬레이션
        final PhotoAnalysisResponse analysisResult =
                photoAnalysisService.analyzeAllPending(schoolId);
        log.info("[2/3] 분석 완료: {}장", analysisResult.getAnalyzedCount());

        // 3단계: 학생 매칭
        final PhotoMatchResponse matchResult =
                photoMatchService.matchStudents(schoolId);
        log.info("[3/3] 매칭 완료: {}건", matchResult.getMatchedCount());

        log.info("=== 파이프라인 완료 ===");

        return PipelineResponse.builder()
                .schoolId(schoolId)
                .uploadedCount(uploadResult.getUploadedCount())
                .analyzedCount(analysisResult.getAnalyzedCount())
                .matchedCount(matchResult.getMatchedCount())
                .message(String.format(
                        "파이프라인 완료: %d장 업로드 → %d장 분석 → %d건 매칭 완료. " +
                        "다음 단계: POST /api/v1/admin/stories/generate?schoolId=%d",
                        uploadResult.getUploadedCount(),
                        analysisResult.getAnalyzedCount(),
                        matchResult.getMatchedCount(),
                        schoolId))
                .build();
    }
}
