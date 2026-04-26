package com.antigravity.domain.order.controller;

import com.antigravity.domain.order.dto.*;
import com.antigravity.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "04. Order & Export", description = "앨범 주문 관리 및 인쇄 파트너 데이터 Export")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "앨범 주문 생성",
               description = "검수 완료된 스토리를 기반으로 앨범 제작 주문을 생성합니다. 하나의 스토리로 하나의 주문만 가능합니다.")
    @PostMapping("/api/v1/admin/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @Operation(summary = "학교 전체 주문 목록 조회",
               description = "학교 단위 주문 현황을 반환합니다. 상태별 집계(summary)를 포함하여 대시보드 통계 카드를 한 번에 렌더링할 수 있습니다.")
    @GetMapping("/api/v1/admin/schools/{schoolId}/orders")
    public ResponseEntity<OrderListResponse> getOrdersBySchool(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(orderService.getOrdersBySchool(schoolId));
    }

    @Operation(summary = "주문 단건 조회", description = "특정 주문의 상세 정보를 반환합니다.")
    @GetMapping("/api/v1/admin/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "주문 ID") @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @Operation(summary = "주문 상태 변경",
               description = "주문 상태를 변경합니다. PENDING → PROCESSING → COMPLETED 순으로 진행합니다.")
    @PatchMapping("/api/v1/admin/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "주문 ID") @PathVariable(name = "orderId") Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    @Operation(summary = "주문 데이터 Export (단건)",
               description = "인쇄 파트너에게 전달할 Story → Chapter → Photo 전체 구조의 JSON을 반환합니다.")
    @GetMapping("/api/v1/admin/orders/{orderId}/export")
    public ResponseEntity<OrderExportResponse> exportOrder(
            @Parameter(description = "주문 ID") @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.exportOrder(orderId));
    }

    @Operation(summary = "학교 전체 주문 일괄 Export",
               description = "학교 전체 주문 데이터를 하나의 JSON으로 반환합니다. 인쇄 파트너에게 학교 단위로 일괄 전달할 때 사용합니다.")
    @GetMapping("/api/v1/admin/schools/{schoolId}/export")
    public ResponseEntity<SchoolExportResponse> exportSchool(
            @Parameter(description = "학교 ID") @PathVariable(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(orderService.exportSchool(schoolId));
    }
}
