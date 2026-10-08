package org.example.orderflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCategory {
    @NotBlank
    private String name;

    public CreateCategory(String name) {
        this.name = name;
    }

    public CreateCategory() {
    }
}
