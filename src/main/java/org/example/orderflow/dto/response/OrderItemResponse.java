package org.example.orderflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.example.orderflow.entity.OrderItem;

@Getter
@Setter
public class OrderItemResponse {

    private Integer productId;
    private String productName;
    private int count;

    public OrderItemResponse(OrderItem item) {
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();
        this.count = item.getCount();
    }
}