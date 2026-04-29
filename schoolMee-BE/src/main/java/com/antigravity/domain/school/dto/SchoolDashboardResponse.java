package com.antigravity.domain.school.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자 대시보드 진입 시 반환되는 학교 정보 + 진행 현황.
 * 한 번의 API 호출로 대시보드 전체를 렌더링할 수 있도록 통계를 포함한다.
 */
@Getter
@Builder
public class SchoolDashboardResponse {
    private Long schoolId;
    private String name;
    private com.antigravity.domain.school.entity.OnboardingStep onboardingStatus;
    private long totalStudents;
    private long storiesGenerated;
    private long ordersCreated;
    private long ordersCompleted;
    private LocalDateTime createdAt;
}
