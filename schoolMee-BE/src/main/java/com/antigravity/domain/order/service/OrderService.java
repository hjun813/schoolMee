package com.antigravity.domain.order.service;

import com.antigravity.domain.order.dto.CreateOrderRequest;
import com.antigravity.domain.order.dto.OrderExportResponse;
import com.antigravity.domain.order.dto.OrderResponse;
import com.antigravity.domain.order.entity.AlbumOrder;
import com.antigravity.domain.order.entity.OrderStatus;
import com.antigravity.domain.order.repository.AlbumOrderRepository;
import com.antigravity.domain.story.entity.Story;
import com.antigravity.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AlbumOrderRepository albumOrderRepository;
    private final StoryRepository storyRepository;

    /**
     * 스토리를 기반으로 앨범 주문을 생성한다.
     * 하나의 Story는 하나의 AlbumOrder만 가질 수 있다. (중복 주문 방지)
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
                .status(OrderStatus.PENDING)  // 주문 초기 상태
                .build();

        return OrderResponse.from(albumOrderRepository.save(order));
    }

    /**
     * 전체 주문 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return albumOrderRepository.findAllWithDetails()
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * 단일 주문을 Export용 전체 구조(Story→Chapter→Photo)로 반환한다.
     * 인쇄 파트너에게 전달되는 최종 JSON 형태.
     */
    @Transactional(readOnly = true)
    public OrderExportResponse exportOrder(final Long orderId) {
        final AlbumOrder order = albumOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        return OrderExportResponse.from(order);
    }

    /**
     * 주문 상태를 변경한다.
     * PENDING → PROCESSING → COMPLETED 순으로 단계적 진행.
     */
    @Transactional
    public OrderResponse updateOrderStatus(final Long orderId, final OrderStatus newStatus) {
        final AlbumOrder order = albumOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));

        order.updateStatus(newStatus);
        // @Transactional 환경에서 Dirty Checking이 동작하므로 save() 불필요
        return OrderResponse.from(order);
    }
}
