package org.example.orderflow.service;

import jakarta.transaction.Transactional;
import org.example.orderflow.dto.response.InventoryResponse;
import org.example.orderflow.entity.Inventory;
import org.example.orderflow.entity.Product;
import org.example.orderflow.exception.ConflictException;
import org.example.orderflow.exception.NotFoundException;
import org.example.orderflow.repository.InventoryRepository;
import org.example.orderflow.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void createInventory(Integer productId, int count) {

        if (count < 0) {
            throw new ConflictException("Количество не может быть отрицательным");
        }

        if (inventoryRepository.existsByProductId(productId)) {
            throw new ConflictException("Остаток для продукта уже существует");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableCount(count);
        inventory.setReservedCount(0);

        inventoryRepository.save(inventory);

        product.setInventory(inventory);
    }

    @Transactional
    public void addStock(Integer productId, int count) {

        if (count <= 0) {
            throw new ConflictException("Количество должно быть больше нуля");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Инвентарь не найден"));

        inventory.setAvailableCount(
                inventory.getAvailableCount() + count
        );
    }

    @Transactional
    public void removeStock(Integer productId, int count) {

        if (count <= 0) {
            throw new ConflictException("Количество должно быть больше нуля");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Инвентарь не найден"));

        if (inventory.getAvailableCount() < count) {
            throw new ConflictException("Недостаточно товара на складе");
        }

        inventory.setAvailableCount(
                inventory.getAvailableCount() - count
        );
    }

    public InventoryResponse getByProductId(Integer productId) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Остаток товара не найден"));

        return new InventoryResponse(inventory);
    }
}