package org.example.orderflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateProductPrice {
    @NotNull
    @Positive
    private BigDecimal price;

    public UpdateProductPrice(BigDecimal price) {
        this.price = price;
    }
}
