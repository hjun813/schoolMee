package com.antigravity.domain.order.repository;

import com.antigravity.domain.order.entity.AlbumOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumOrderRepository extends JpaRepository<AlbumOrder, Long> {

    // 주문 목록 조회 시 student, school, story 모두 fetch (N+1 방지)
    @Query("SELECT o FROM AlbumOrder o " +
           "JOIN FETCH o.student st " +
           "JOIN FETCH st.school " +
           "JOIN FETCH o.story")
    List<AlbumOrder> findAllWithDetails();

    // Export용 단건 조회: story의 chapters/photos 전체 포함
    @Query("SELECT o FROM AlbumOrder o " +
           "JOIN FETCH o.student st " +
           "JOIN FETCH st.school " +
           "JOIN FETCH o.story s " +
           "LEFT JOIN FETCH s.chapters c " +
           "LEFT JOIN FETCH c.chapterPhotos cp " +
           "LEFT JOIN FETCH cp.photo " +
           "WHERE o.id = :orderId")
    Optional<AlbumOrder> findByIdWithDetails(@Param("orderId") Long orderId);

    boolean existsByStoryId(Long storyId);
}
