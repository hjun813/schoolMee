package com.antigravity.domain.school.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OnboardingStep {
    SCHOOL_CREATED("학교 정보 등록됨"),
    CLASS_CREATED("반 정보 등록됨"),
    STUDENT_UPLOADED("학생 정보 및 증명사진 등록됨"),
    PHOTO_UPLOADED("단체 사진 등록됨"),
    MATCHING_COMPLETED("매칭 완료"),
    STORY_GENERATED("스토리 생성 완료 - 온보딩 완료");

    private final String description;
}
