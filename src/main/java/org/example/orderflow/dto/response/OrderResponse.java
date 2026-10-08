package org.example.orderflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.example.orderflow.entity.Order;
import org.example.orderflow.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {

    private Integer id;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();

        this.items = order.getItems()
                .stream()
                .map(OrderItemResponse::new)
                .toList();
    }
}