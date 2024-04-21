package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - 엔티티가 변하면 API 스펙이 변함
     * - 트랜잭션 안에서 지연 로딩 필요
     * - 양방향 연관관계 문제
     * @return List<Order>
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {

        List<Order> all = orderRepository.findAll(new OrderSearch());
        for(Order order : all) {

            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화

        }

        return all;

    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환
     * - 트랜잭션 안에서 지연 로딩 필요
     * @return List<OrderDto>
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {

        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;

    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환
     * - fetch join 최적화
     * @return List<OrderDto>
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {

        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;

    }

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     *  * - ToOne 관계만 우선 모두 페치 조인으로 최적화
     *  * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
     * @return List<OrderDto>
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "100") int limit){

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;

    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }

    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }

    }

    /**
     * V4. JPA에서 DTO 직접 조회
     * @return List<OrderQueryDto>
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {

        return orderQueryRepository.findOrderQueryDtos();

    }

    /**
     * V5. JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
     * @return List<OrderQueryDto>
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {

        return orderQueryRepository.findAllByDto_optimization();

    }

}
