package org.example.orderflow.service;


import jakarta.transaction.Transactional;
import org.example.orderflow.dto.CreateOrderRequest;
import org.example.orderflow.dto.OrderItemRequest;
import org.example.orderflow.dto.response.OrderResponse;
import org.example.orderflow.entity.*;
import org.example.orderflow.enums.OrderStatus;
import org.example.orderflow.exception.ConflictException;
import org.example.orderflow.exception.ForbiddenException;
import org.example.orderflow.exception.NotFoundException;
import org.example.orderflow.repository.InventoryRepository;
import org.example.orderflow.repository.OrderRepository;
import org.example.orderflow.repository.ProductRepository;
import org.example.orderflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public class OrderService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(InventoryRepository inventoryRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createOrder(String username, CreateOrderRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setUser(user);

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Товар не найден"));

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Товара нет на складе"));

            if (inventory.getAvailableCount() < itemRequest.getCount()) {
                throw new ConflictException("Товара недостаточно на складе");
            }

            inventory.setAvailableCount(
                    inventory.getAvailableCount() - itemRequest.getCount()
            );

            inventory.setReservedCount(
                    inventory.getReservedCount() + itemRequest.getCount()
            );

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setCount(itemRequest.getCount());

            order.getItems().add(orderItem);
        }

        orderRepository.save(order);
    }
    @Transactional
    public void cancelOrder(Integer orderId, String username) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));
        if (!order.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Нет доступа к этому заказу");
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Этот заказ нельзя отменить");
        }

        for (OrderItem item : order.getItems()) {

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Остаток товара не найден"));

            inventory.setAvailableCount(
                    inventory.getAvailableCount() + item.getCount()
            );

            inventory.setReservedCount(
                    inventory.getReservedCount() - item.getCount()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
    }
    public Page<OrderResponse> getMyOrders(String username, Pageable pageable) {

        return orderRepository
                .findAllByUserUsername(username, pageable)
                .map(OrderResponse::new);
    }
    public OrderResponse getMyOrderById(Integer orderId, String username) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Нет доступа к этому заказу");
        }

        return new OrderResponse(order);
    }
    public Page<OrderResponse> getAllOrders(Pageable pageable) {

        return orderRepository
                .findAll(pageable)
                .map(OrderResponse::new);
    }

}