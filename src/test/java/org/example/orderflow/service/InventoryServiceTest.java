
package org.example.orderflow.service;

import org.example.orderflow.entity.Inventory;
import org.example.orderflow.entity.Product;
import org.example.orderflow.exception.ConflictException;
import org.example.orderflow.exception.NotFoundException;
import org.example.orderflow.repository.InventoryRepository;
import org.example.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    private InventoryService inventoryService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                inventoryRepository,
                productRepository
        );

        product = new Product();
        product.setId(1);

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableCount(10);
        inventory.setReservedCount(0);
    }

    @Test
    void createInventory_success() {
        when(inventoryRepository.existsByProductId(1))
                .thenReturn(false);

        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));

        inventoryService.createInventory(1, 10);

        ArgumentCaptor<Inventory> captor =
                ArgumentCaptor.forClass(Inventory.class);

        verify(inventoryRepository).save(captor.capture());

        Inventory saved = captor.getValue();

        assertEquals(product, saved.getProduct());
        assertEquals(10, saved.getAvailableCount());
        assertEquals(0, saved.getReservedCount());

        assertEquals(saved, product.getInventory());
    }

    @Test
    void createInventory_alreadyExists() {
        when(inventoryRepository.existsByProductId(1))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> inventoryService.createInventory(1, 10)
        );

        verify(productRepository, never()).findById(anyInt());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_productNotFound() {
        when(inventoryRepository.existsByProductId(1))
                .thenReturn(false);

        when(productRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> inventoryService.createInventory(1, 10)
        );

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void addStock_success() {
        when(inventoryRepository.findByProductId(1))
                .thenReturn(Optional.of(inventory));

        inventoryService.addStock(1, 5);

        assertEquals(15, inventory.getAvailableCount());
    }

    @Test
    void addStock_invalidCount() {
        assertThrows(
                ConflictException.class,
                () -> inventoryService.addStock(1, 0)
        );

        verify(inventoryRepository, never()).findByProductId(anyInt());
    }

    @Test
    void removeStock_success() {
        when(inventoryRepository.findByProductId(1))
                .thenReturn(Optional.of(inventory));

        inventoryService.removeStock(1, 4);

        assertEquals(6, inventory.getAvailableCount());
    }

    @Test
    void removeStock_notEnoughStock() {
        when(inventoryRepository.findByProductId(1))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                ConflictException.class,
                () -> inventoryService.removeStock(1, 11)
        );

        assertEquals(10, inventory.getAvailableCount());
    }

    @Test
    void removeStock_inventoryNotFound() {
        when(inventoryRepository.findByProductId(1))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> inventoryService.removeStock(1, 2)
        );
    }
}