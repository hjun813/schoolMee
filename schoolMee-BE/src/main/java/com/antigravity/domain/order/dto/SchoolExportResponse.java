package com.antigravity.domain.order.dto;

import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학교 전체 주문 일괄 Export 응답.
 * 인쇄 파트너에게 전달할 전체 학교 주문 데이터를 한 번에 담는 구조.
 */
@Getter
@Builder
public class SchoolExportResponse {
    private Long schoolId;
    private String schoolName;
    private LocalDateTime exportedAt;
    private int totalOrders;
    private List<OrderExportResponse> orders;

    public static SchoolExportResponse of(final Long schoolId,
                                          final String schoolName,
                                          final List<AlbumOrder> orders) {
        return SchoolExportResponse.builder()
                .schoolId(schoolId)
                .schoolName(schoolName)
                .exportedAt(LocalDateTime.now())
                .totalOrders(orders.size())
                .orders(orders.stream().map(OrderExportResponse::from).toList())
                .build();
    }
}
