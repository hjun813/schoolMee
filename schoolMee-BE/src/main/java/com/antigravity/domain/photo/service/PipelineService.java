package com.antigravity.domain.photo.service;

import com.antigravity.domain.photo.dto.PhotoAnalysisResponse;
import com.antigravity.domain.photo.dto.PhotoMatchResponse;
import com.antigravity.domain.photo.dto.PipelineStatusResponse;
import com.antigravity.domain.photo.entity.PipelineJob;
import com.antigravity.domain.photo.entity.PipelineJobStatus;
import com.antigravity.domain.photo.repository.PipelineJobRepository;
import com.antigravity.domain.story.dto.StoryGenerateResponse;
import com.antigravity.domain.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사진 처리 통합 파이프라인 (비동기 대응).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    private final PhotoAnalysisService photoAnalysisService;
    private final PhotoMatchService photoMatchService;
    private final StoryService storyService;
    private final PipelineJobRepository pipelineJobRepository;

    /**
     * 비동기 파이프라인 시작. 즉시 jobId를 반환한다.
     */
    @Transactional
    public PipelineStatusResponse startPipeline(List<Long> photoIds, Long schoolId) {
        log.info("▶ 비동기 파이프라인 요청 접수: 학교 {}", schoolId);
        
        PipelineJob job = PipelineJob.builder()
                .schoolId(schoolId)
                .status(PipelineJobStatus.PENDING)
                .totalCount(photoIds != null ? photoIds.size() : 0)
                .build();
        
        PipelineJob savedJob = pipelineJobRepository.save(job);
        
        // 실제 연산은 백그라운드 스레드에서 실행
        runAsyncPipeline(savedJob.getId(), photoIds, schoolId);
        
        return PipelineStatusResponse.from(savedJob);
    }

    /**
     * 실제 연산 수행 로직 (@Async)
     */
    @Async
    @Transactional
    public void runAsyncPipeline(Long jobId, List<Long> photoIds, Long schoolId) {
        PipelineJob job = pipelineJobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            job.updateStatus(PipelineJobStatus.PROCESSING);
            pipelineJobRepository.saveAndFlush(job);
            
            log.info("▶ [Job {}] 파이프라인 비동기 실행 시작", jobId);

            // 1. 분석 (Analyzed)
            final PhotoAnalysisResponse analysisResult = photoAnalysisService.analyzePhotos(photoIds);
            log.info("[Job {}] 1/3 분석 완료: {}장", jobId, analysisResult.getProcessedCount());

            // 2. 매칭 (PhotoStudent)
            final PhotoMatchResponse matchResult = photoMatchService.matchStudents(photoIds);
            log.info("[Job {}] 2/3 매칭 완료: {}명 매칭", jobId, matchResult.getMatchedStudentCount());

            // 3. 스토리 일괄 생성
            final StoryGenerateResponse storyResult = storyService.generateStoriesForSchool(schoolId);
            log.info("[Job {}] 3/3 스토리 생성 완료: {}건 생성", jobId, storyResult.getGenerated());

            job.complete(analysisResult.getProcessedCount(), storyResult.getGenerated());
            pipelineJobRepository.save(job);
            log.info("=== [Job {}] 파이프라인 비동기 완료 ===", jobId);

        } catch (Exception e) {
            log.error("=== [Job {}] 파이프라인 비동기 실행 중 오류 발생 ===", jobId, e);
            job.fail(e.getMessage());
            pipelineJobRepository.save(job);
        }
    }

    @Transactional(readOnly = true)
    public PipelineStatusResponse getJobStatus(Long jobId) {
        PipelineJob job = pipelineJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("작업 정보를 찾을 수 없습니다. jobId=" + jobId));
        return PipelineStatusResponse.from(job);
    }
}
