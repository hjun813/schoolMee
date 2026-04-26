package com.antigravity.domain.student.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 학생 상세 조회 응답.
 * 스토리 요약(chapterCount, photoCount)을 포함하여
 * 상세 화면 진입 전 카드 형태로 빠르게 렌더링할 수 있다.
 */
@Getter
@Builder
public class StudentDetailResponse {
    private Long studentId;
    private String name;
    private Integer grade;
    private Integer classNum;
    private String schoolName;
    private Long storyId;       // null이면 스토리 미생성
    private String storyTitle;
    private int chapterCount;
    private int photoCount;
    private boolean hasOrder;
}
