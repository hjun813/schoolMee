package com.antigravity.domain.photo.service;

import com.antigravity.domain.photo.dto.PhotoMatchResponse;
import com.antigravity.domain.photo.entity.AnalysisStatus;
import com.antigravity.domain.photo.entity.Photo;
import com.antigravity.domain.photo.entity.PhotoStudent;
import com.antigravity.domain.photo.repository.PhotoRepository;
import com.antigravity.domain.photo.repository.PhotoStudentRepository;
import com.antigravity.domain.student.entity.Student;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 사진-학생 매칭 서비스.
 *
 * [매칭 알고리즘]
 * - 각 ANALYZED 사진의 detectedFacesCount(n)만큼 학생을 랜덤 선택
 * - matchScore = (smileScore * 0.6 + activityScore * 0.4) / 100.0  → 0.0~1.0
 * - (photo_id, student_id) 복합 UK로 중복 매칭 자동 방지
 *
 * [실제 서비스 교체 포인트]
 * - Collections.shuffle() → 얼굴 임베딩 코사인 유사도 기반 정렬로 교체
 * - matchScore → 유사도 점수로 교체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoMatchService {

    private final PhotoRepository photoRepository;
    private final PhotoStudentRepository photoStudentRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public PhotoMatchResponse matchStudents(Long schoolId) {
        final List<Photo> analyzedPhotos =
                photoRepository.findBySchoolIdAndAnalysisStatus(schoolId, AnalysisStatus.ANALYZED);
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);

        if (students.isEmpty()) {
            throw new IllegalStateException("매칭할 학생이 없습니다. schoolId=" + schoolId);
        }

        if (analyzedPhotos.isEmpty()) {
            log.warn("ANALYZED 상태 사진 없음 — 먼저 /photos/analyze 를 호출하세요. schoolId={}", schoolId);
            return PhotoMatchResponse.builder()
                    .matchedCount(0)
                    .matches(List.of())
                    .build();
        }

        final List<PhotoMatchResponse.PhotoMatchItem> matchItems = new ArrayList<>();

        for (Photo photo : analyzedPhotos) {
            // 이 사진에서 감지된 얼굴 수만큼 학생 선택 (최대 전체 학생 수까지)
            final int faceCount  = photo.getDetectedFacesCount() != null ? photo.getDetectedFacesCount() : 1;
            final int selectCount = Math.min(faceCount, students.size());

            // 학생 리스트를 섞어 랜덤으로 selectCount명 선택
            final List<Student> shuffled = new ArrayList<>(students);
            Collections.shuffle(shuffled);
            final List<Student> selected = shuffled.subList(0, selectCount);

            for (Student student : selected) {
                // 복합 UK 확인 — 중복 매칭 방지
                if (photoStudentRepository.existsByPhotoIdAndStudentId(photo.getId(), student.getId())) {
                    log.debug("중복 매칭 스킵: photoId={}, studentId={}", photo.getId(), student.getId());
                    continue;
                }

                final double matchScore = computeMatchScore(photo);

                photoStudentRepository.save(PhotoStudent.builder()
                        .photo(photo)
                        .student(student)
                        .matchScore(matchScore)
                        .build());

                matchItems.add(PhotoMatchResponse.PhotoMatchItem.builder()
                        .photoId(photo.getId())
                        .studentId(student.getId())
                        .studentName(student.getName())
                        .matchScore(matchScore)
                        .build());

                log.info("매칭 완료: photoId={}, student={}({}), score={}",
                        photo.getId(), student.getName(), student.getId(), matchScore);
            }
        }

        log.info("총 {}건 매칭 완료 (schoolId={})", matchItems.size(), schoolId);

        return PhotoMatchResponse.builder()
                .matchedCount(matchItems.size())
                .matches(matchItems)
                .build();
    }

    /**
     * 매칭 신뢰도 점수 계산.
     * matchScore = (smileScore × 0.6 + activityScore × 0.4) / 100.0
     * null-safe 처리: null인 경우 50 기본값 사용
     */
    private double computeMatchScore(Photo photo) {
        final int smile    = photo.getSmileScore()    != null ? photo.getSmileScore()    : 50;
        final int activity = photo.getActivityScore() != null ? photo.getActivityScore() : 50;
        // 소수점 2자리 반올림
        return Math.round((smile * 0.6 + activity * 0.4)) / 100.0;
    }
}
