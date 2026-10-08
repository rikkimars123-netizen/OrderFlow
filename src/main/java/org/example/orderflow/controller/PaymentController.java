package org.example.orderflow.controller;

import org.example.orderflow.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<Void> createPayment(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        paymentService.createPayment(
                orderId,
                userDetails.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{orderId}/success")
    public ResponseEntity<Void> successPayment(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        paymentService.successPayment(
                orderId,
                userDetails.getUsername()
        );
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}/failed")
    public ResponseEntity<Void> failedPayment(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        paymentService.failedPayment(
                orderId,
                userDetails.getUsername()
        );
        return ResponseEntity.ok().build();
    }
}