package org.example.orderflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.example.orderflow.entity.Product;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductCatalogResponse {

    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer categoryId;
    private String categoryName;

    public ProductCatalogResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();

        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getName();
    }
}