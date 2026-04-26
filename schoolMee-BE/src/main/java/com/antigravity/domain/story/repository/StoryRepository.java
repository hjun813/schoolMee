package com.antigravity.domain.story.repository;

import com.antigravity.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    // 학생별 스토리 상세 (Chapter + Photo 포함) - N+1 방지
    @Query("SELECT s FROM Story s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.school " +
           "WHERE s.student.id = :studentId")
    List<Story> findAllByStudentIdWithDetails(@Param("studentId") Long studentId);

    // 학교 단위 스토리 목록 (Chapter 수만 파악 - 목록 화면용)
    @Query("SELECT s FROM Story s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.school " +
           "WHERE st.school.id = :schoolId")
    List<Story> findAllBySchoolIdWithChapters(@Param("schoolId") Long schoolId);

    // Export용 단건 전체 구조 로드
    @Query("SELECT s FROM Story s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.school " +
           "LEFT JOIN FETCH s.chapters c " +
           "LEFT JOIN FETCH c.chapterPhotos cp " +
           "LEFT JOIN FETCH cp.photo " +
           "WHERE s.id = :storyId")
    Optional<Story> findByIdWithDetails(@Param("storyId") Long storyId);

    boolean existsByStudentId(Long studentId);

    long countByStudentSchoolId(Long schoolId);
}
