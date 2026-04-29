package com.antigravity.domain.order.repository;

import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumOrderRepository extends JpaRepository<AlbumOrder, Long> {

    // 학교 단위 주문 목록 (N+1 방지)
    @Query("SELECT o FROM AlbumOrder o " +
           "JOIN FETCH o.student st " +
           "JOIN FETCH st.classRoom cr " +
           "JOIN FETCH cr.school " +
           "JOIN FETCH o.story " +
           "WHERE cr.school.id = :schoolId")
    List<AlbumOrder> findAllBySchoolIdWithDetails(@Param("schoolId") Long schoolId);

    // Export용 단건 전체 구조 (chapters/photos 전체 포함)
    @Query("SELECT o FROM AlbumOrder o " +
           "JOIN FETCH o.student st " +
           "JOIN FETCH st.classRoom cr " +
           "JOIN FETCH cr.school " +
           "JOIN FETCH o.story s " +
           "LEFT JOIN FETCH s.chapters c " +
           "LEFT JOIN FETCH c.chapterPhotos cp " +
           "LEFT JOIN FETCH cp.photo " +
           "WHERE o.id = :orderId")
    Optional<AlbumOrder> findByIdWithDetails(@Param("orderId") Long orderId);

    // 학교 전체 Export용 (chapters/photos 포함)
    @Query("SELECT DISTINCT o FROM AlbumOrder o " +
           "JOIN FETCH o.student st " +
           "JOIN FETCH st.classRoom cr " +
           "JOIN FETCH cr.school " +
           "JOIN FETCH o.story s " +
           "LEFT JOIN FETCH s.chapters c " +
           "LEFT JOIN FETCH c.chapterPhotos cp " +
           "LEFT JOIN FETCH cp.photo " +
           "WHERE cr.school.id = :schoolId")
    List<AlbumOrder> findAllBySchoolIdWithFullDetails(@Param("schoolId") Long schoolId);

    Optional<AlbumOrder> findByStoryId(Long storyId);
    boolean existsByStoryId(Long storyId);
    boolean existsByStudentId(Long studentId);

    long countByStudentClassRoomSchoolIdAndStatus(Long schoolId, OrderStatus status);

    long countByStudentClassRoomSchoolId(Long schoolId);
}
