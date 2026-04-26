package com.antigravity.domain.student.service;

import com.antigravity.domain.order.repository.AlbumOrderRepository;
import com.antigravity.domain.story.repository.StoryRepository;
import com.antigravity.domain.student.dto.StudentDetailResponse;
import com.antigravity.domain.student.dto.StudentListResponse;
import com.antigravity.domain.student.dto.StudentListResponse.StudentItem;
import com.antigravity.domain.student.entity.Student;
import com.antigravity.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StoryRepository storyRepository;
    private final AlbumOrderRepository albumOrderRepository;

    /**
     * 학교 소속 학생 목록 조회.
     * hasStory, hasOrder 플래그를 포함하여 프론트에서 버튼 활성화 여부를 판단할 수 있다.
     */
    public StudentListResponse getStudentsBySchool(final Long schoolId) {
        final List<Student> students = studentRepository.findAllBySchoolIdWithSchool(schoolId);

        final List<StudentItem> items = students.stream()
                .map(s -> StudentItem.builder()
                        .studentId(s.getId())
                        .name(s.getName())
                        .grade(s.getGrade())
                        .classNum(s.getClassNum())
                        .hasStory(storyRepository.existsByStudentId(s.getId()))
                        .hasOrder(albumOrderRepository.existsByStudentId(s.getId()))
                        .build())
                .toList();

        return StudentListResponse.builder()
                .schoolId(schoolId)
                .students(items)
                .build();
    }

    /**
     * 학생 상세 조회.
     * 스토리 요약 정보(chapterCount, photoCount)를 포함하여 상세 카드 화면에서 사용.
     */
    public StudentDetailResponse getStudentDetail(final Long studentId) {
        final Student student = studentRepository.findByIdWithSchool(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. studentId=" + studentId));

        // 스토리 조회 (없으면 null 허용)
        final var stories = storyRepository.findAllByStudentIdWithDetails(studentId);
        final var story = stories.isEmpty() ? null : stories.get(0);

        final int photoCount = story == null ? 0 :
                story.getChapters().stream().mapToInt(c -> c.getChapterPhotos().size()).sum();

        final boolean hasOrder = story != null && albumOrderRepository.existsByStoryId(story.getId());

        return StudentDetailResponse.builder()
                .studentId(student.getId())
                .name(student.getName())
                .grade(student.getGrade())
                .classNum(student.getClassNum())
                .schoolName(student.getSchool().getName())
                .storyId(story == null ? null : story.getId())
                .storyTitle(story == null ? null : story.getTitle())
                .chapterCount(story == null ? 0 : story.getChapters().size())
                .photoCount(photoCount)
                .hasOrder(hasOrder)
                .build();
    }
}
