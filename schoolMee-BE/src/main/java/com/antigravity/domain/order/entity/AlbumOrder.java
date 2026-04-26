package com.antigravity.domain.order.entity;

import com.antigravity.domain.story.entity.Story;
import com.antigravity.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 앨범 주문 엔티티.
 * 학생 1명의 Story를 기반으로 생성되는 물리적 앨범 제작 주문.
 * 주문 상태(OrderStatus)를 통해 진행 단계를 추적한다.
 */
@Entity
@Table(name = "album_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlbumOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 대상 학생
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 주문의 원본 콘텐츠 (Story → Chapter → Photo 구조 포함)
    // Story와 1:1 관계: 하나의 Story로 하나의 앨범만 제작 가능
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false, unique = true)
    private Story story;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 상태 변경 메서드 (도메인 로직)
    public void updateStatus(final OrderStatus newStatus) {
        this.status = newStatus;
    }
}
