package org.example.orderflow.service;

import org.example.orderflow.dto.CreateOrderRequest;
import org.example.orderflow.dto.OrderItemRequest;
import org.example.orderflow.entity.Inventory;
import org.example.orderflow.entity.Order;
import org.example.orderflow.entity.OrderItem;
import org.example.orderflow.entity.Product;
import org.example.orderflow.entity.User;
import org.example.orderflow.enums.OrderStatus;
import org.example.orderflow.exception.ConflictException;
import org.example.orderflow.exception.ForbiddenException;
import org.example.orderflow.exception.NotFoundException;
import org.example.orderflow.repository.InventoryRepository;
import org.example.orderflow.repository.OrderRepository;
import org.example.orderflow.repository.ProductRepository;
import org.example.orderflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private OrderService orderService;

    private User user;
    private Product product;
    private Inventory inventory;

    private final String username = "anton";

    @BeforeEach
    void setUp() {

        orderService = new OrderService(
                inventoryRepository,
                productRepository,
                orderRepository,
                userRepository
        );

        user = new User();
        user.setUsername(username);

        product = new Product();
        product.setId(1);
        product.setName("iPhone");

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableCount(10);
        inventory.setReservedCount(0);
    }

    private CreateOrderRequest createRequest(int count) {

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1);
        item.setCount(count);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));

        return request;
    }

    @Test
    void createOrder_success() {

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductIdForUpdate(1))
                .thenReturn(Optional.of(inventory));

        orderService.createOrder(username, createRequest(3));

        assertEquals(7, inventory.getAvailableCount());
        assertEquals(3, inventory.getReservedCount());

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertEquals(user, savedOrder.getUser());
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());

        assertEquals(1, savedOrder.getItems().size());
        assertEquals(product, savedOrder.getItems().get(0).getProduct());
        assertEquals(3, savedOrder.getItems().get(0).getCount());
    }

    @Test
    void createOrder_notEnoughStock() {

        inventory.setAvailableCount(2);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductIdForUpdate(1))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                ConflictException.class,
                () -> orderService.createOrder(username, createRequest(3))
        );

        assertEquals(2, inventory.getAvailableCount());
        assertEquals(0, inventory.getReservedCount());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_productNotFound() {

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> orderService.createOrder(username, createRequest(1))
        );

        verify(orderRepository, never()).save(any());
        verify(inventoryRepository, never())
                .findByProductIdForUpdate(anyInt());
    }

    @Test
    void cancelOrder_success() {

        Order order = createExistingOrder(user, OrderStatus.CREATED, 3);

        inventory.setAvailableCount(7);
        inventory.setReservedCount(3);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductIdForUpdate(1))
                .thenReturn(Optional.of(inventory));

        orderService.cancelOrder(1, username);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, inventory.getAvailableCount());
        assertEquals(0, inventory.getReservedCount());
    }

    @Test
    void cancelOrder_foreignUser() {

        Order order = createExistingOrder(user, OrderStatus.CREATED, 3);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        assertThrows(
                ForbiddenException.class,
                () -> orderService.cancelOrder(1, "anotherUser")
        );

        assertEquals(OrderStatus.CREATED, order.getStatus());

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(anyInt());
    }

    @Test
    void cancelOrder_wrongStatus() {

        Order order = createExistingOrder(
                user,
                OrderStatus.COMPLETED,
                3
        );

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        assertThrows(
                ConflictException.class,
                () -> orderService.cancelOrder(1, username)
        );

        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(anyInt());
    }

    private Order createExistingOrder(
            User owner,
            OrderStatus status,
            int count
    ) {

        Order order = new Order();
        order.setId(1);
        order.setUser(owner);
        order.setStatus(status);
        order.setItems(new ArrayList<>());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setCount(count);

        order.getItems().add(item);

        return order;
    }
}