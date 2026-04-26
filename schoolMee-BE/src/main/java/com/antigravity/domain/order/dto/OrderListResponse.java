package com.antigravity.domain.order.dto;

import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 학교 단위 주문 목록 응답.
 * summary(상태별 집계)를 포함하여 프론트엔드 대시보드 상단 통계 카드를
 * 별도 API 없이 한 번에 렌더링할 수 있다.
 */
@Getter
@Builder
public class OrderListResponse {
    private Long schoolId;
    private Map<String, Long> summary;   // {"PENDING": 3, "PROCESSING": 2, "COMPLETED": 5}
    private List<OrderItem> orders;

    @Getter
    @Builder
    public static class OrderItem {
        private Long orderId;
        private Long studentId;
        private String studentName;
        private Long storyId;
        private String storyTitle;
        private OrderStatus status;
        private LocalDateTime createdAt;

        public static OrderItem from(final AlbumOrder order) {
            return OrderItem.builder()
                    .orderId(order.getId())
                    .studentId(order.getStudent().getId())
                    .studentName(order.getStudent().getName())
                    .storyId(order.getStory().getId())
                    .storyTitle(order.getStory().getTitle())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }
}
