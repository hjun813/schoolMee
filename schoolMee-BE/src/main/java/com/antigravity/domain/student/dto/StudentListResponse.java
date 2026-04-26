package com.antigravity.domain.student.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 학교 소속 학생 목록 응답.
 * hasStory, hasOrder 플래그를 포함하여 프론트엔드에서
 * 추가 API 호출 없이 다음 단계 버튼 활성화/비활성화를 판단할 수 있다.
 */
@Getter
@Builder
public class StudentListResponse {
    private Long schoolId;
    private List<StudentItem> students;

    @Getter
    @Builder
    public static class StudentItem {
        private Long studentId;
        private String name;
        private Integer grade;
        private Integer classNum;
        private boolean hasStory;   // 스토리 생성 여부 → "스토리 보기" 버튼 활성화 기준
        private boolean hasOrder;   // 주문 생성 여부 → "주문하기" 버튼 비활성화 기준
    }
}
