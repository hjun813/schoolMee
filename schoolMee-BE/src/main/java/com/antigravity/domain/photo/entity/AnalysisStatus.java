package com.antigravity.domain.photo.entity;

/**
 * 사진 AI 분석 상태.
 * PENDING  : 업로드 완료, 분석 대기 중
 * ANALYZED : AI 시뮬레이션 분석 완료
 */
public enum AnalysisStatus {
    PENDING,
    ANALYZED
}
