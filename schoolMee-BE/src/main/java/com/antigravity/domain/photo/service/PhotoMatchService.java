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

import java.util.List;
import java.util.stream.Collectors;

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
    public PhotoMatchResponse matchStudents(List<Long> photoIds) {
        final List<Photo> groupPhotos = photoRepository.findAllById(photoIds)
                .stream()
                .filter(p -> p.getAnalysisStatus() == AnalysisStatus.ANALYZED && p.getType() == com.antigravity.domain.photo.entity.PhotoType.GROUP)
                .collect(Collectors.toList());

        if (groupPhotos.isEmpty()) {
            return PhotoMatchResponse.builder().processedPhotoCount(0).matchedStudentCount(0).build();
        }

        Long schoolId = groupPhotos.get(0).getSchool().getId();
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);
        if (students.isEmpty()) {
            throw new IllegalStateException("매칭할 학생이 없습니다. schoolId=" + schoolId);
        }

        int totalMatchedStudents = 0;
        final java.util.Random RANDOM = new java.util.Random();

        for (Photo photo : groupPhotos) {
            for (Student student : students) {
                // 70% 확률로 매칭 수행
                if (RANDOM.nextDouble() <= 0.7) {
                    if (!photoStudentRepository.existsByPhotoIdAndStudentId(photo.getId(), student.getId())) {
                        double matchScore = computeMatchScore(photo);
                        photoStudentRepository.save(PhotoStudent.builder()
                                .photo(photo)
                                .student(student)
                                .matchScore(matchScore)
                                .build());
                        totalMatchedStudents++;
                    }
                }
            }
        }

        // 온보딩 단계 업데이트
        com.antigravity.domain.school.entity.School school = groupPhotos.get(0).getSchool();
        if (school.getOnboardingStep() == com.antigravity.domain.school.entity.OnboardingStep.PHOTO_UPLOADED) {
            school.updateOnboardingStep(com.antigravity.domain.school.entity.OnboardingStep.MATCHING_COMPLETED);
            log.info("학교 온보딩 단계 업데이트: {} -> {}", 
                com.antigravity.domain.school.entity.OnboardingStep.PHOTO_UPLOADED, 
                com.antigravity.domain.school.entity.OnboardingStep.MATCHING_COMPLETED);
        }

        return PhotoMatchResponse.builder()
                .processedPhotoCount(groupPhotos.size())
                .matchedStudentCount(totalMatchedStudents)
                .build();
    }

    private double computeMatchScore(Photo photo) {
        final int smile    = photo.getSmileScore()    != null ? photo.getSmileScore()    : 50;
        final int activity = photo.getActivityScore() != null ? photo.getActivityScore() : 50;
        return (smile * 0.6 + activity * 0.4) / 100.0;
    }
}
