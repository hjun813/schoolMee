package com.antigravity.domain.story.repository;

import com.antigravity.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    // 학생의 스토리 조회 시 chapters와 chapterPhotos, photo까지 한 번에 fetch
    // N+1 문제 방지: 단일 JPQL로 모든 연관 데이터를 로드
    @Query("SELECT DISTINCT s FROM Story s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.school " +
           "LEFT JOIN FETCH s.chapters c " +
           "LEFT JOIN FETCH c.chapterPhotos cp " +
           "LEFT JOIN FETCH cp.photo " +
           "WHERE s.student.id = :studentId")
    List<Story> findAllByStudentIdWithDetails(@Param("studentId") Long studentId);

    // Export용: 특정 스토리의 전체 구조를 한 번에 로드
    @Query("SELECT s FROM Story s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.school " +
           "LEFT JOIN FETCH s.chapters c " +
           "LEFT JOIN FETCH c.chapterPhotos cp " +
           "LEFT JOIN FETCH cp.photo " +
           "WHERE s.id = :storyId")
    Optional<Story> findByIdWithDetails(@Param("storyId") Long storyId);

    boolean existsByStudentId(Long studentId);
}
