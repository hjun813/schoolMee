package com.antigravity.domain.school.service;

import com.antigravity.domain.order.entity.OrderStatus;
import com.antigravity.domain.order.repository.AlbumOrderRepository;
import com.antigravity.domain.photo.dto.PhotoPoolResponse;
import com.antigravity.domain.photo.repository.PhotoRepository;
import com.antigravity.domain.school.dto.SchoolDashboardResponse;
import com.antigravity.domain.school.entity.School;
import com.antigravity.domain.school.repository.SchoolRepository;
import com.antigravity.domain.story.repository.StoryRepository;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StoryRepository storyRepository;
    private final AlbumOrderRepository albumOrderRepository;
    private final PhotoRepository photoRepository;

    /**
     * 관리자 대시보드 진입 시 호출.
     * 학교 기본 정보 + 학생/스토리/주문 통계를 한 번의 쿼리 조합으로 반환.
     */
    public SchoolDashboardResponse getDashboard(final Long schoolId) {
        final School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("학교를 찾을 수 없습니다. schoolId=" + schoolId));

        final long totalStudents = studentRepository.countBySchoolId(schoolId);
        final long storiesGenerated = storyRepository.countByStudentSchoolId(schoolId);
        final long ordersCreated = albumOrderRepository.countByStudentSchoolId(schoolId);
        final long ordersCompleted = albumOrderRepository.countByStudentSchoolIdAndStatus(schoolId, OrderStatus.COMPLETED);

        return SchoolDashboardResponse.builder()
                .schoolId(school.getId())
                .name(school.getName())
                .totalStudents(totalStudents)
                .storiesGenerated(storiesGenerated)
                .ordersCreated(ordersCreated)
                .ordersCompleted(ordersCompleted)
                .createdAt(school.getCreatedAt())
                .build();
    }

    /**
     * 학교 사진 풀 현황 조회.
     * 스토리 생성 전 사진이 충분한지 확인하는 용도.
     */
    public PhotoPoolResponse getPhotoPool(final Long schoolId) {
        return PhotoPoolResponse.of(schoolId, photoRepository.findBySchoolId(schoolId));
    }
}
