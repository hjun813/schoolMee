package com.antigravity.domain.school.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 학교 엔티티.
 * SchoolMee의 B2B 계약 단위. 모든 학생/사진 데이터는 School에 귀속된다.
 */
@Entity
@Table(name = "schools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_step", nullable = false)
    @Builder.Default
    private OnboardingStep onboardingStep = OnboardingStep.SCHOOL_CREATED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateOnboardingStep(OnboardingStep step) {
        this.onboardingStep = step;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
