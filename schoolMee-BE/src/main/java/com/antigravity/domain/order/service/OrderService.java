package com.antigravity.domain.order.service;

import com.antigravity.domain.order.dto.*;
import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import com.antigravity.domain.order.repository.AlbumOrderRepository;
import com.antigravity.domain.school.entity.School;
import com.antigravity.domain.school.repository.SchoolRepository;
import com.antigravity.domain.story.entity.Story;
import com.antigravity.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AlbumOrderRepository albumOrderRepository;
    private final StoryRepository storyRepository;
    private final SchoolRepository schoolRepository;

    /**
     * 스토리를 기반으로 앨범 주문 생성.
     * 하나의 Story → 하나의 AlbumOrder (중복 방지).
     */
    @Transactional
    public OrderResponse createOrder(final CreateOrderRequest request) {
        if (albumOrderRepository.existsByStoryId(request.getStoryId())) {
            throw new IllegalStateException("이미 해당 스토리로 생성된 주문이 있습니다. storyId=" + request.getStoryId());
        }

        final Story story = storyRepository.findByIdWithDetails(request.getStoryId())
                .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다. storyId=" + request.getStoryId()));

        final AlbumOrder order = AlbumOrder.builder()
                .student(story.getStudent())
                .story(story)
                .status(OrderStatus.PENDING)
                .build();

        return OrderResponse.from(albumOrderRepository.save(order));
    }

    /**
     * 학교 단위 전체 주문 목록 + 상태별 summary 조회.
     * 대시보드 통계 카드를 별도 API 없이 한 번에 렌더링 가능.
     */
    @Transactional(readOnly = true)
    public OrderListResponse getOrdersBySchool(final Long schoolId) {
        final List<AlbumOrder> orders = albumOrderRepository.findAllBySchoolIdWithDetails(schoolId);

        // 상태별 집계
        final Map<String, Long> summary = Map.of(
                "PENDING",    orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count(),
                "PROCESSING", orders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count(),
                "COMPLETED",  orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count()
        );

        return OrderListResponse.builder()
                .schoolId(schoolId)
                .summary(summary)
                .orders(orders.stream().map(OrderListResponse.OrderItem::from).toList())
                .build();
    }

    /**
     * 주문 단건 조회.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(final Long orderId) {
        final AlbumOrder order = albumOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));
        return OrderResponse.from(order);
    }

    /**
     * 주문 상태 변경. PENDING → PROCESSING → COMPLETED.
     * Dirty Checking으로 별도 save() 호출 불필요.
     */
    @Transactional
    public OrderResponse updateOrderStatus(final Long orderId, final UpdateOrderStatusRequest request) {
        final AlbumOrder order = albumOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));
        order.updateStatus(request.getStatus());
        return OrderResponse.from(order);
    }

    /**
     * 단건 Export: 인쇄 파트너 전달용 전체 구조 JSON.
     */
    @Transactional(readOnly = true)
    public OrderExportResponse exportOrder(final Long orderId) {
        final AlbumOrder order = albumOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));
        return OrderExportResponse.from(order);
    }

    /**
     * 학교 전체 주문 일괄 Export.
     * 인쇄 파트너에게 학교 단위로 한 번에 전달하는 용도.
     */
    @Transactional(readOnly = true)
    public SchoolExportResponse exportSchool(final Long schoolId) {
        final School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("학교를 찾을 수 없습니다. schoolId=" + schoolId));
        final List<AlbumOrder> orders = albumOrderRepository.findAllBySchoolIdWithFullDetails(schoolId);
        return SchoolExportResponse.of(schoolId, school.getName(), orders);
    }
}
