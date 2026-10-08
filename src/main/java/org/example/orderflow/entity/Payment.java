package org.example.orderflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.orderflow.enums.PaymentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

}
