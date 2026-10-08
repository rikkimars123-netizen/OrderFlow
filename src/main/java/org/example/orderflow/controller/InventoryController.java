package org.example.orderflow.controller;

import jakarta.validation.constraints.Positive;
import org.example.orderflow.dto.response.InventoryResponse;
import org.example.orderflow.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Validated
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Integer productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> createInventory(
            @PathVariable Integer productId,
            @RequestParam @Positive int count) {

        inventoryService.createInventory(productId, count);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/add")
    public ResponseEntity<Void> addStock(
            @PathVariable Integer productId,
            @RequestParam @Positive int count) {
        inventoryService.addStock(productId, count);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/remove")
    public ResponseEntity<Void> removeStock(
            @PathVariable Integer productId,
            @RequestParam @Positive int count) {
        inventoryService.removeStock(productId, count);
        return ResponseEntity.ok().build();
    }
}
