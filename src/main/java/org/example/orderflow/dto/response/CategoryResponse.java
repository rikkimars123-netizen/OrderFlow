package org.example.orderflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.example.orderflow.entity.Category;
@Getter
@Setter
public class CategoryResponse {
    private Integer id;
    private String name;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}
