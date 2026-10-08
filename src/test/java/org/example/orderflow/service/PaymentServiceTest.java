package org.example.orderflow.service;

import org.example.orderflow.entity.*;
import org.example.orderflow.enums.OrderStatus;
import org.example.orderflow.enums.PaymentStatus;
import org.example.orderflow.exception.ConflictException;
import org.example.orderflow.exception.ForbiddenException;
import org.example.orderflow.repository.InventoryRepository;
import org.example.orderflow.repository.OrderRepository;
import org.example.orderflow.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    private PaymentService paymentService;

    private User user;
    private Order order;
    private Product product;
    private Inventory inventory;
    private Payment payment;

    private final String username = "anton";

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                orderRepository,
                paymentRepository,
                inventoryRepository
        );

        user = new User();
        user.setUsername(username);

        product = new Product();
        product.setId(1);

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableCount(7);
        inventory.setReservedCount(3);

        order = new Order();
        order.setId(1);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setItems(new ArrayList<>());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setCount(3);

        order.getItems().add(item);

        payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void createPayment_success() {
        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        paymentService.createPayment(1, username);

        ArgumentCaptor<Payment> captor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository).save(captor.capture());

        Payment savedPayment = captor.getValue();

        assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());
        assertEquals(order, savedPayment.getOrder());
        assertEquals(savedPayment, order.getPayment());
    }

    @Test
    void createPayment_alreadyExists() {
        order.setPayment(payment);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        assertThrows(
                ConflictException.class,
                () -> paymentService.createPayment(1, username)
        );

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_foreignUser() {
        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        assertThrows(
                ForbiddenException.class,
                () -> paymentService.createPayment(1, "other")
        );

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void successPayment_success() {
        order.setPayment(payment);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1))
                .thenReturn(Optional.of(payment));

        when(inventoryRepository.findByProductIdForUpdate(1))
                .thenReturn(Optional.of(inventory));

        paymentService.successPayment(1, username);

        assertEquals(0, inventory.getReservedCount());
        assertEquals(7, inventory.getAvailableCount());

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void successPayment_wrongPaymentStatus() {
        payment.setStatus(PaymentStatus.SUCCESS);
        order.setPayment(payment);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1))
                .thenReturn(Optional.of(payment));

        assertThrows(
                ConflictException.class,
                () -> paymentService.successPayment(1, username)
        );

        verify(inventoryRepository, never())
                .findByProductIdForUpdate(anyInt());
    }

    @Test
    void failedPayment_success() {
        order.setPayment(payment);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1))
                .thenReturn(Optional.of(payment));

        when(inventoryRepository.findByProductIdForUpdate(1))
                .thenReturn(Optional.of(inventory));

        paymentService.failedPayment(1, username);

        assertEquals(0, inventory.getReservedCount());
        assertEquals(10, inventory.getAvailableCount());

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void failedPayment_foreignUser() {
        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        assertThrows(
                ForbiddenException.class,
                () -> paymentService.failedPayment(1, "other")
        );

        verify(paymentRepository, never()).findByOrderId(anyInt());
        verify(inventoryRepository, never())
                .findByProductIdForUpdate(anyInt());
    }

    @Test
    void successPayment_invalidReserve() {
        order.setPayment(payment);
        inventory.setReservedCount(2);

        when(orderRepository.findById(1))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1))
                .thenReturn(Optional.of(payment));

        when(inventoryRepository.findByProductIdForUpdate(1))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                ConflictException.class,
                () -> paymentService.successPayment(1, username)
        );

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }
}