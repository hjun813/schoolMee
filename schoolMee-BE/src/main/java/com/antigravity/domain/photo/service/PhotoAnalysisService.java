package com.antigravity.domain.photo.service;

import com.antigravity.domain.photo.dto.PhotoAnalysisResponse;
import com.antigravity.domain.photo.entity.AnalysisStatus;
import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    @Transactional
    public PhotoAnalysisResponse analyzePhotos(List<Long> photoIds) {
        final List<Photo> pendingPhotos = photoRepository.findAllById(photoIds)
                .stream()
                .filter(p -> p.getAnalysisStatus() == AnalysisStatus.PENDING)
                .collect(Collectors.toList());

        if (pendingPhotos.isEmpty()) {
            return PhotoAnalysisResponse.builder().processedCount(0).build();
        }

        int processedCount = 0;
        for (Photo photo : pendingPhotos) {
            final int smileScore = 40 + RANDOM.nextInt(61);
            final int activityScore = 30 + RANDOM.nextInt(71);
            final int faceCount = 2 + RANDOM.nextInt(4); // 2~5명

            List<String> faces = new java.util.ArrayList<>();
            for (int i = 1; i <= faceCount; i++) {
                faces.add("\"detected_face_" + i + "\"");
            }
            final String faceIds = "[" + String.join(",", faces) + "]";

            photo.applyAnalysisResult(smileScore, activityScore, faceCount, faceIds);
            processedCount++;
        }

        return PhotoAnalysisResponse.builder()
                .processedCount(processedCount)
                .build();
    }
}
