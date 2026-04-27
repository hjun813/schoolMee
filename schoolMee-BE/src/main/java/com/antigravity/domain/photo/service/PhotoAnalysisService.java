package com.antigravity.domain.photo.service;

import com.antigravity.domain.photo.dto.PhotoAnalysisResponse;
import com.antigravity.domain.photo.entity.AnalysisStatus;
import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 사진 AI 분석 서비스 (시뮬레이션).
 *
 * 실제 AI 대신 가중치 기반 랜덤으로 아래 값을 생성한다:
 * - smileScore    : 40~100 (균일 분포)
 * - activityScore : 30~100 (균일 분포)
 * - detectedFacesCount : 1~5 (균일 분포)
 * - faceIds : "face_1,face_2,..." (detectedFacesCount 수만큼)
 *
 * @Transactional 내에서 Photo.applyAnalysisResult()를 호출하므로
 * dirty checking으로 자동 UPDATE 발생. 별도 save() 불필요.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoAnalysisService {

    private final PhotoRepository photoRepository;

    private static final Random RANDOM = new Random();

    /**
     * 학교 내 PENDING 상태 사진 전체를 분석한다.
     * 이미 ANALYZED인 사진은 건너뜀.
     */
    @Transactional
    public PhotoAnalysisResponse analyzeAllPending(Long schoolId) {
        final List<Photo> pendingPhotos =
                photoRepository.findBySchoolIdAndAnalysisStatus(schoolId, AnalysisStatus.PENDING);

        if (pendingPhotos.isEmpty()) {
            log.info("PENDING 상태 사진 없음. schoolId={}", schoolId);
            return PhotoAnalysisResponse.builder()
                    .analyzedCount(0)
                    .results(List.of())
                    .build();
        }

        final List<PhotoAnalysisResponse.PhotoAnalysisItem> results = new ArrayList<>();

        for (Photo photo : pendingPhotos) {
            // AI 시뮬레이션: 랜덤 분석 결과 생성
            final int smileScore    = 40 + RANDOM.nextInt(61);  // 40~100
            final int activityScore = 30 + RANDOM.nextInt(71);  // 30~100
            final int faceCount     = 1  + RANDOM.nextInt(5);   // 1~5

            final String faceIds = IntStream.rangeClosed(1, faceCount)
                    .mapToObj(i -> "face_" + i)
                    .collect(Collectors.joining(","));

            // dirty checking으로 자동 persist (@Transactional 범위 내)
            photo.applyAnalysisResult(smileScore, activityScore, faceCount, faceIds);

            results.add(PhotoAnalysisResponse.PhotoAnalysisItem.builder()
                    .photoId(photo.getId())
                    .smileScore(smileScore)
                    .activityScore(activityScore)
                    .detectedFacesCount(faceCount)
                    .faceIds(faceIds)
                    .status(AnalysisStatus.ANALYZED.name())
                    .build());

            log.info("사진 분석 완료: photoId={}, smile={}, activity={}, faces={}",
                    photo.getId(), smileScore, activityScore, faceCount);
        }

        log.info("총 {}장 분석 완료 (schoolId={})", results.size(), schoolId);

        return PhotoAnalysisResponse.builder()
                .analyzedCount(results.size())
                .results(results)
                .build();
    }
}
