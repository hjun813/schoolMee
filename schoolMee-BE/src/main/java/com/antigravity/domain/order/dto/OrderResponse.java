package com.antigravity.domain.order.dto;

import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private Long studentId;
    private String studentName;
    private Integer grade;
    private Integer classNum;
    private String schoolName;
    private Long storyId;
    private String storyTitle;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public static OrderResponse from(final AlbumOrder order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .studentId(order.getStudent().getId())
                .studentName(order.getStudent().getName())
                .grade(order.getStudent().getClassRoom().getGrade())
                .classNum(order.getStudent().getClassRoom().getClassNum())
                .schoolName(order.getStudent().getSchool().getName())
                .storyId(order.getStory().getId())
                .storyTitle(order.getStory().getTitle())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
