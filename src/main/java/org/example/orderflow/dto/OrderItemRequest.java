package org.example.orderflow.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemRequest {
    @NotNull
    private Integer productId;
    @NotNull
    @Positive
    private int count;

    public OrderItemRequest(Integer productId, int count) {
        this.productId = productId;
        this.count = count;
    }

    public OrderItemRequest() {}
}
