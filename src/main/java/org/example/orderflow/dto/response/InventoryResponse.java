package org.example.orderflow.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.example.orderflow.entity.Inventory;

@Getter
@Setter
public class InventoryResponse {

    private Integer id;
    private Integer productId;
    private int availableCount;
    private int reservedCount;

    public InventoryResponse(Inventory inventory) {
        this.id = inventory.getId();
        this.productId = inventory.getProduct().getId();
        this.availableCount = inventory.getAvailableCount();
        this.reservedCount = inventory.getReservedCount();
    }
}
