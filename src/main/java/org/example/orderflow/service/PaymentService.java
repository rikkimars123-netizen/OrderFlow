package org.example.orderflow.service;

import jakarta.transaction.Transactional;
import org.example.orderflow.entity.Inventory;
import org.example.orderflow.entity.Order;
import org.example.orderflow.entity.OrderItem;
import org.example.orderflow.entity.Payment;
import org.example.orderflow.enums.OrderStatus;
import org.example.orderflow.enums.PaymentStatus;
import org.example.orderflow.exception.ConflictException;
import org.example.orderflow.exception.ForbiddenException;
import org.example.orderflow.exception.NotFoundException;
import org.example.orderflow.repository.InventoryRepository;
import org.example.orderflow.repository.OrderRepository;
import org.example.orderflow.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;

    public PaymentService(OrderRepository orderRepository,
                          PaymentRepository paymentRepository,
                          InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryRepository = inventoryRepository;
    }


    @Transactional
    public void createPayment(Integer orderId, String username) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));
        if (!order.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Нет доступа к этому заказу");
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Оплату нельзя создать для этого заказа");
        }

        if (order.getPayment() != null) {
            throw new ConflictException("Оплата для заказа уже существует");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);
        order.setPayment(payment);
    }
    @Transactional
    public void successPayment(Integer orderId, String username) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));
        if (!order.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Нет доступа к этому заказу");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Платеж не найден"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Заказ нельзя оплатить");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Нельзя провести оплату");
        }

        for (OrderItem item : order.getItems()) {

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() ->
                            new NotFoundException("Остаток товара не найден"));

            if (inventory.getReservedCount() < item.getCount()) {
                throw new ConflictException("Некорректный резерв товара");
            }

            inventory.setReservedCount(
                    inventory.getReservedCount() - item.getCount()
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        order.setStatus(OrderStatus.COMPLETED);
    }

    @Transactional
    public void failedPayment(Integer orderId, String username) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));
        if (!order.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Нет доступа к этому заказу");
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Платеж не найден"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Заказ уже завершен или отменен");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Платеж уже обработан");
        }

        for (OrderItem item : order.getItems()) {

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() ->
                            new NotFoundException("Остаток товара не найден"));

            if (inventory.getReservedCount() < item.getCount()) {
                throw new ConflictException("Некорректный резерв товара");
            }

            inventory.setReservedCount(
                    inventory.getReservedCount() - item.getCount()
            );

            inventory.setAvailableCount(
                    inventory.getAvailableCount() + item.getCount()
            );
        }

        payment.setStatus(PaymentStatus.FAILED);
        order.setStatus(OrderStatus.CANCELLED);
    }

}
