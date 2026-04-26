package com.antigravity.domain.story.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * AI 스토리 일괄 생성 결과 응답.
 * 멱등성 보장: 이미 스토리가 있는 학생은 건너뛰고 skipped 카운트.
 */
@Getter
@Builder
public class StoryGenerateResponse {
    private Long schoolId;
    private int generated;
    private int skipped;
    private String message;
}
