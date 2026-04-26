package com.antigravity.domain.order.dto;

import com.antigravity.domain.order.entity.OrderStatus;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}
