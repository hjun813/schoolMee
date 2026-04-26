package com.antigravity.domain.admin.controller;

import com.antigravity.domain.order.dto.CreateOrderRequest;
import com.antigravity.domain.order.dto.OrderExportResponse;
import com.antigravity.domain.order.dto.OrderResponse;
import com.antigravity.domain.order.entity.OrderStatus;
import com.antigravity.domain.order.service.OrderService;
import com.antigravity.domain.story.dto.StoryResponse;
import com.antigravity.domain.story.service.StoryService;
import com.antigravity.domain.student.dto.StudentResponse;
import com.antigravity.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API", description = "SchoolMee 관리자 전용 API")
public class AdminController {

    private final StudentService studentService;
    private final StoryService storyService;
    private final OrderService orderService;

    @Operation(summary = "전체 학생 목록 조회", description = "등록된 모든 학생 정보를 반환합니다.")
    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @Operation(summary = "학교 전체 스토리 일괄 생성 (AI 시뮬레이션)", description = "지정한 학교의 사진 풀을 기반으로 모든 학생에 대해 AI 스토리를 자동 생성합니다.")
    @PostMapping("/stories/generate")
    public ResponseEntity<List<StoryResponse>> generateStories(
            @Parameter(description = "스토리를 생성할 학교 ID") @RequestParam(name = "schoolId") Long schoolId) {
        return ResponseEntity.ok(storyService.generateStoriesForSchool(schoolId));
    }

    @Operation(summary = "학생별 스토리 조회", description = "특정 학생의 스토리(챕터 + 사진 포함)를 반환합니다.")
    @GetMapping("/stories/{studentId}")
    public ResponseEntity<List<StoryResponse>> getStories(
            @Parameter(description = "학생 ID") @PathVariable(name = "studentId") Long studentId) {
        return ResponseEntity.ok(storyService.getStoriesByStudent(studentId));
    }

    @Operation(summary = "앨범 주문 생성", description = "스토리를 기반으로 앨범 제작 주문을 생성합니다.")
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @Operation(summary = "전체 주문 목록 조회")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "주문 상태 변경", description = "orderId의 주문 상태를 변경합니다.")
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "주문 ID") @PathVariable(name = "orderId") Long orderId,
            @Parameter(description = "변경할 상태") @RequestParam(name = "status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @Operation(summary = "주문 데이터 Export (인쇄 파트너용)", description = "Story → Chapter → Photo 전체 구조를 JSON으로 반환합니다.")
    @GetMapping("/orders/{orderId}/export")
    public ResponseEntity<OrderExportResponse> exportOrder(
            @Parameter(description = "주문 ID") @PathVariable(name = "orderId") Long orderId) {
        return ResponseEntity.ok(orderService.exportOrder(orderId));
    }
}
