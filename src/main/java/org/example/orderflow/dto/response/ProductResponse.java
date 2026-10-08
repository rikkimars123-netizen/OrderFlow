package org.example.orderflow.dto.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orderflow.entity.Product;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {

    private Integer id;
    private String name;
    private BigDecimal price;

    private Integer categoryId;
    private String categoryName;

    private Integer availableCount;
    private Integer reservedCount;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();

        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getName();

        if (product.getInventory() != null) {
            this.availableCount = product.getInventory().getAvailableCount();
            this.reservedCount = product.getInventory().getReservedCount();
        }
    }
}
