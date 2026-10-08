package org.example.orderflow.controller;

import jakarta.validation.Valid;
import org.example.orderflow.dto.CreateOrderRequest;
import org.example.orderflow.dto.response.OrderResponse;
import org.example.orderflow.service.OrderService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        orderService.createOrder(
                userDetails.getUsername(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        orderService.cancelOrder(
                orderId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok().build();
    }
    @GetMapping
    public Page<OrderResponse> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @ParameterObject Pageable pageable) {

        return orderService.getMyOrders(
                userDetails.getUsername(),
                pageable
        );
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrderById(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                orderService.getMyOrderById(
                        orderId,
                        userDetails.getUsername()
                )
        );
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public Page<OrderResponse> getAllOrders(
            @ParameterObject Pageable pageable) {

        return orderService.getAllOrders(pageable);
    }
}