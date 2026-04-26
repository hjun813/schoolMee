package com.antigravity.domain.order.entity;

/**
 * 앨범 주문 상태 Enum.
 * PENDING    → 주문 접수됨, 처리 대기 중
 * PROCESSING → 인쇄 작업 진행 중
 * COMPLETED  → 제작 완료
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED
}
